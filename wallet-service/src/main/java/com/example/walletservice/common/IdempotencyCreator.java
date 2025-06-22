package com.example.walletservice.common;

import java.util.UUID;

public abstract class IdempotencyCreator {

    private IdempotencyCreator() {}

    public static String createIdempotencyKey(Object any){
        return UUID.nameUUIDFromBytes(
                any.toString().getBytes()
        ).toString();
    }
}
