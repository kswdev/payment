package com.example.backend.domain;

import lombok.Getter;

public enum PSPConfirmationStatus {
    DONE("완료"),
    CANCEL("승인된 결제가 취소된 상태"),
    EXPIRED("승인된 결제가 만료된 상태"),
    PARTIAL_CANCELED("승인된 결제가 부분 취소된 상태"),
    ABORTED("결제가 승인이 실패된 상태"),
    WAITING_FOR_DEPOSIT("가상계좌 결제 흐름에 있는 상태, 입금 대기"),
    IN_PROGRESS("결제 수단 정보와 결제 수단 정보의 소유자가 맞는지 인증을 마친 상태"),
    READY("결제를 생성하면 가지는 초기 상")
    ;

    PSPConfirmationStatus(String description) {
        this.description = description;
    }

    @Getter private final String description;
}
