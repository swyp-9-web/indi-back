package com.swyp.artego.global.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return "[]"; // 또는 null로 저장하고 싶으면 return null;
        }
        try {
            return mapper.writeValueAsString(dataList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("List → JSON 변환 실패", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
        if (data == null || data.trim().isEmpty()) {
            return Collections.emptyList(); // 또는 null 반환하고 싶다면 return null;
        }
        try {
            return mapper.readValue(data, List.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON → List 변환 실패", e);
        }
    }
}
