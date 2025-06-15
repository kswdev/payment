package com.example.backend.adapter.out.stream.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public abstract class Mapper {
    public static final ObjectMapper om = new ObjectMapper().configure(WRITE_DATES_AS_TIMESTAMPS, false);

    // TypeReference를 위한 정적 상수 추가
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {};

    // 유틸리티 메서드 추가
    public static Map<String, Object> readAsMap(String json) {
        try {
            return om.readValue(json, MAP_TYPE_REFERENCE);
        } catch (Exception e) {
            throw new RuntimeException("JSON을 Map으로 변환하는 중 오류 발생", e);
        }
    }

}
