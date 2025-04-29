package com.swyp.artego.domain.item.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * Spring의 multipart/form-data 요청 처리 시 발생하는
 * 'Content-Type: application/octet-stream' 문제를 방지하기 위한 커스텀 HttpMessageConverter
 *
 * 이 컨버터는 application/octet-stream 타입에 대해 JSON 변환을 시도하지 않도록 설정함으로써
 * MultipartFile과 JSON을 함께 사용할 때 발생하는 Jackson 처리 오류를 회피한다.
 *
 * 참고: multipart/form-data 요청에서 JSON과 파일을 함께 전송할 경우,
 * Jackson이 application/octet-stream을 처리하려고 시도하며 오류가 발생할 수 있음.
 */
@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(MediaType mediaType) {
        return false;
    }
}