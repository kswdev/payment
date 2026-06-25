package net.study.ledgerservice.ledger.application.service;

import net.study.ledgerservice.ledger.application.port.out.DuplicateMessageFilterPort;
import net.study.ledgerservice.ledger.application.port.out.LoadAccountPort;
import net.study.ledgerservice.ledger.application.port.out.LoadPaymentOrderPort;
import net.study.ledgerservice.ledger.application.port.out.SaveDoubleLedgerEntryPort;
import net.study.ledgerservice.ledger.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoubleLedgerEntryRecordServiceTest {

    @Mock
    private DuplicateMessageFilterPort duplicateMessageFilter;

    @Mock
    private LoadAccountPort loadAccountPort;

    @Mock
    private LoadPaymentOrderPort loadPaymentOrderPort;

    @Mock
    private SaveDoubleLedgerEntryPort saveDoubleLedgerEntryPort;

    @InjectMocks
    private DoubleLedgerEntryRecordService service;

    @DisplayName("recordDoubleLedgerEntry - 중복 메시지가 아닌 경우 복식부기 저장 성공")
    @Test
    void recordDoubleLedgerEntry_save_payment_event() {
        //given
        PaymentEventMessage paymentEventMessage = paymentEventMessage();
        Account revenueAccount = new Account(1L, "REVENUE");
        Account buyerAccount = new Account(2L, "ITEM_BUYER");
        PaymentOrder paymentOrder = new PaymentOrder(10L, 5000L, "order-001");

        given(duplicateMessageFilter.isAlreadyProcess(paymentEventMessage)).willReturn(Boolean.FALSE);
        given(loadAccountPort.getAccount("REVENUE")).willReturn(revenueAccount);
        given(loadAccountPort.getAccount("ITEM_BUYER")).willReturn(buyerAccount);
        given(loadPaymentOrderPort.loadPaymentOrder("order-001")).willReturn(List.of(paymentOrder));

        //when
        LedgerEventMessage ledgerEventMessage = service.recordDoubleLedgerEntry(paymentEventMessage);

        //then
        verify(duplicateMessageFilter).isAlreadyProcess(paymentEventMessage);
        verify(loadAccountPort).getAccount("REVENUE");
        verify(loadAccountPort).getAccount("ITEM_BUYER");
        verify(loadPaymentOrderPort).loadPaymentOrder(paymentEventMessage.getOrderId());
        verify(saveDoubleLedgerEntryPort).saveDoubleLedgerEntries(any());

        assertThat(ledgerEventMessage.getType()).isEqualTo(LedgerEventMessage.Type.SUCCESS);
        assertThat(ledgerEventMessage.getPayload()).isEqualTo(paymentEventMessage.getPayload());
        assertThat(ledgerEventMessage.getMetadata()).isEqualTo(paymentEventMessage.getMetadata());
    }

    @DisplayName("recordDoubleLedgerEntry - 중복 메시지인 경우 복식부기 저장 스킵")
    @Test
    void recordDoubleLedgerEntry_skip_when_duplicate_message() {
        //given
        PaymentEventMessage paymentEventMessage = paymentEventMessage();
        given(duplicateMessageFilter.isAlreadyProcess(paymentEventMessage)).willReturn(Boolean.TRUE);

        //when
        LedgerEventMessage ledgerEventMessage = service.recordDoubleLedgerEntry(paymentEventMessage);

        //then
        verify(duplicateMessageFilter).isAlreadyProcess(paymentEventMessage);
        verifyNoInteractions(loadAccountPort);
        verifyNoInteractions(loadPaymentOrderPort);
        verifyNoInteractions(saveDoubleLedgerEntryPort);

        assertThat(ledgerEventMessage.getType()).isEqualTo(LedgerEventMessage.Type.SUCCESS);
        assertThat(ledgerEventMessage.getPayload()).isEqualTo(paymentEventMessage.getPayload());
        assertThat(ledgerEventMessage.getMetadata()).isEqualTo(paymentEventMessage.getMetadata());
    }

    @DisplayName("recordDoubleLedgerEntry - 복식부기 항목의 credit/debit/transaction 내용 검증")
    @Test
    void recordDoubleLedgerEntry_creates_correct_double_ledger_entry_content() {
        //given
        PaymentEventMessage paymentEventMessage = paymentEventMessage();
        Account revenueAccount = new Account(1L, "REVENUE");
        Account buyerAccount = new Account(2L, "ITEM_BUYER");
        PaymentOrder paymentOrder = new PaymentOrder(10L, 5000L, "order-001");

        given(duplicateMessageFilter.isAlreadyProcess(paymentEventMessage)).willReturn(Boolean.FALSE);
        given(loadAccountPort.getAccount("REVENUE")).willReturn(revenueAccount);
        given(loadAccountPort.getAccount("ITEM_BUYER")).willReturn(buyerAccount);
        given(loadPaymentOrderPort.loadPaymentOrder("order-001")).willReturn(List.of(paymentOrder));

        //when
        service.recordDoubleLedgerEntry(paymentEventMessage);

        //then
        verify(saveDoubleLedgerEntryPort).saveDoubleLedgerEntries(
                argThat(entries -> {
                    if (entries.size() != 1) return false;

                    DoubleLedgerEntry entry = entries.get(0);

                    return entry.getCredit().getAccount().equals(buyerAccount) &&
                            entry.getCredit().getAmount() == 5000L &&
                            entry.getCredit().getType() == LedgerEntry.LedgerEntryType.CREDIT &&
                            entry.getDebit().getAccount().equals(revenueAccount) &&
                            entry.getDebit().getAmount() == 5000L &&
                            entry.getDebit().getType() == LedgerEntry.LedgerEntryType.DEBIT &&
                            entry.getTransaction().getReferenceType() == ReferenceType.PAYMENT_ORDER &&
                            entry.getTransaction().getReferenceId() == 10L &&
                            entry.getTransaction().getOrderId() == "order-001";
                }));
    }

    @DisplayName("recordDoubleLedgerEntry - 여러 PaymentOrder에 대해 각각 복식부기 항목 생성")
    @Test
    void recordDoubleLedgerEntry_creates_multiple_double_ledger_entries() {
        //given
        PaymentEventMessage paymentEventMessage = paymentEventMessage();
        Account revenueAccount = new Account(1L, "REVENUE");
        Account buyerAccount = new Account(2L, "ITEM_BUYER");
        PaymentOrder paymentOrder1 = new PaymentOrder(10L, 3000L, "order-001");
        PaymentOrder paymentOrder2 = new PaymentOrder(11L, 7000L, "order-001");

        given(duplicateMessageFilter.isAlreadyProcess(paymentEventMessage)).willReturn(Boolean.FALSE);
        given(loadAccountPort.getAccount("REVENUE")).willReturn(revenueAccount);
        given(loadAccountPort.getAccount("ITEM_BUYER")).willReturn(buyerAccount);
        given(loadPaymentOrderPort.loadPaymentOrder("order-001")).willReturn(List.of(paymentOrder1, paymentOrder2));

        //when
        service.recordDoubleLedgerEntry(paymentEventMessage);

        //then
        verify(saveDoubleLedgerEntryPort).saveDoubleLedgerEntries(
                argThat(entries -> {
                    if (entries.size() != 2) return  false;

                    return entries.get(0).getCredit().getAmount() == 3000L &&
                            entries.get(0).getTransaction().getReferenceId() == 10L &&
                            entries.get(1).getCredit().getAmount() == 7000L &&
                            entries.get(1).getTransaction().getReferenceId() == 11L;
                })
        );
    }

    private PaymentEventMessage paymentEventMessage() {
        return new PaymentEventMessage(
                PaymentEventMessage.Type.PAYMENT_CONFIRMATION_SUCCESS,
                Map.of("orderId", "order-001"),
                Map.of("partitionKey", 2));
    }
}
