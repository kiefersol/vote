package com.example.vote;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            System.err.println("JSON 변환 실패: " + e.getMessage());
            return "{}";
        }
    }
}
