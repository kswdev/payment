package net.study.ledgerservice.ledger.application.service;

import lombok.RequiredArgsConstructor;
import net.study.ledgerservice.common.UseCase;
import net.study.ledgerservice.ledger.application.port.in.DoubleLedgerEntryRecordUseCase;
import net.study.ledgerservice.ledger.application.port.out.DuplicateMessageFilterPort;
import net.study.ledgerservice.ledger.application.port.out.LoadAccountPort;
import net.study.ledgerservice.ledger.application.port.out.LoadPaymentOrderPort;
import net.study.ledgerservice.ledger.application.port.out.SaveDoubleLedgerEntryPort;
import net.study.ledgerservice.ledger.domain.*;

import java.util.List;

import static net.study.ledgerservice.ledger.domain.LedgerEntry.*;

@RequiredArgsConstructor
@UseCase
public class DoubleLedgerEntryRecordService implements DoubleLedgerEntryRecordUseCase {

    private final String REVENUE_ACCOUNT_NAME = "REVENUE";
    private final String ITEM_BUYER_ACCOUNT_NAME = "ITEM_BUYER";

    private final DuplicateMessageFilterPort duplicateMessageFilter;
    private final SaveDoubleLedgerEntryPort saveDoubleLedgerEntryPort;
    private final LoadPaymentOrderPort loadPaymentOrderPort;
    private final LoadAccountPort loadAccountPort;

    @Override
    public LedgerEventMessage recordDoubleLedgerEntry(PaymentEventMessage message) {
        if (duplicateMessageFilter.isAlreadyProcess(message)) {
            return createLedgerEventMessage(message);
        }

        DoubleAccountsForLedger doubleAccountsForLedger = getDoubleAccountsForLedger();
        List<PaymentOrder> paymentOrders = loadPaymentOrderPort.loadPaymentOrder(message.getOrderId());
        List<DoubleLedgerEntry> doubleLedgerEntries = createDoubleLedgerEntries(doubleAccountsForLedger, paymentOrders);

        saveDoubleLedgerEntryPort.saveDoubleLedgerEntries(doubleLedgerEntries);

        return createLedgerEventMessage(message);
    }

    private DoubleAccountsForLedger getDoubleAccountsForLedger() {
        Account revenueAccount = loadAccountPort.getAccount(REVENUE_ACCOUNT_NAME);
        Account buyerAccount = loadAccountPort.getAccount(ITEM_BUYER_ACCOUNT_NAME);
        return new DoubleAccountsForLedger(revenueAccount, buyerAccount);
    }

    private List<DoubleLedgerEntry> createDoubleLedgerEntries(DoubleAccountsForLedger doubleAccountsForLedger, List<PaymentOrder> paymentOrders) {
        return paymentOrders.stream()
                .map(item -> DoubleLedgerEntry.of(
                        new LedgerEntry(doubleAccountsForLedger.getTo(), item.getAmount(), LedgerEntryType.CREDIT),
                        new LedgerEntry(doubleAccountsForLedger.getFrom(), item.getAmount(), LedgerEntryType.DEBIT),
                        new LedgerTransaction(item.getReferenceType(), item.getId(), item.getOrderId())
                )).toList();
    }

    private LedgerEventMessage createLedgerEventMessage(PaymentEventMessage message) {
        return new LedgerEventMessage(
                LedgerEventMessage.Type.SUCCESS,
                message.getPayload(),
                message.getMetadata());
    }
}
