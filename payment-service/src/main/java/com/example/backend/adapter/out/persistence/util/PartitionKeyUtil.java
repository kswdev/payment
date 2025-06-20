package com.example.backend.adapter.out.persistence.util;

public abstract class PartitionKeyUtil {

    // payment topic partition number
    private static final int PARTITION_KEY_COUNT = 6;

    public static int createPartitionKey(int number) {
        return Math.abs(number) % PARTITION_KEY_COUNT ;
    }
}
