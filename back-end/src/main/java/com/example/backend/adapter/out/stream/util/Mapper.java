package com.example.backend.adapter.out.stream.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public abstract class Mapper {
    public static final ObjectMapper om = new ObjectMapper().configure(WRITE_DATES_AS_TIMESTAMPS, false);
}
