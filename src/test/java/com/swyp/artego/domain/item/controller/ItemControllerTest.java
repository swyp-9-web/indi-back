package com.swyp.artego.domain.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.response.ItemCreateResponse;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.item.service.ItemService;
import com.swyp.artego.global.auth.oauth.dto.response.NaverOAuth2Response;
import com.swyp.artego.global.auth.oauth.dto.response.OAuth2Response;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.common.code.SuccessCode;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ncp.storage.bucket.folder.item-post=test-folder"
})
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Validator validator;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Value("${ncp.storage.bucket.folders.item-post}")
    private String folderName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuthUser testUser;

    @TestConfiguration
    static class ValidatorConfig {
        @Bean
        public Validator validator() {
            return Validation.buildDefaultValidatorFactory().getValidator();
        }
    }

    @BeforeEach
    void setup() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "1234567890");
        userInfo.put("email", "testuser@naver.com");
        userInfo.put("name", "Test User");
        userInfo.put("mobile", "010-1234-5678");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", userInfo);

        OAuth2Response naverResponse = new NaverOAuth2Response(attributes);
        testUser = new AuthUser(naverResponse, "ROLE_USER");
    }

    @Test
    void createItem() throws Exception {
        // given
        ItemCreateRequest request = ItemCreateRequest.builder()
                .title("title")
                .categoryType(CategoryType.TEXTILE_ART)
                .size(new ItemCreateRequest.ItemSize(10,20,30))
                .material("material")
                .description("description")
                .price(10000)
                .secret(false)
                .statusType(StatusType.OPEN)
                .build();

        ItemCreateResponse mockResponse = new ItemCreateResponse(1L); // 응답 객체 생성

        when(itemService.createItem(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(mockResponse);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "images", "image1.jpg", "image/jpeg", "dummy image".getBytes()
        );

        // when + then
        mockMvc.perform(multipart("/api/v1/items/")
                        .file(requestPart)
                        .file(imagePart)
                        .with(user(testUser)) // 시큐리티 설정 (AuthUser는 UserDetails 구현체여야 함)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SuccessCode.INSERT_SUCCESS.getStatus()));
    }

    @Test
    void createItem_shouldReturnBadRequest_whenTooManyImagesProvided() throws Exception {
        // given
        ItemCreateRequest request = new ItemCreateRequest();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartHttpServletRequestBuilder multipartRequest = (MockMultipartHttpServletRequestBuilder) multipart("/api/v1/items/")
                .file(requestPart)
                .with(user(testUser))
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA);

        // 이미지 9개 추가 (8개 초과)
        List<MockMultipartFile> imageFiles = IntStream.range(0, 9)
                .mapToObj(i -> new MockMultipartFile(
                        "images", "image" + i + ".jpg", "image/jpeg", "dummy image".getBytes()
                ))
                .toList();
        for (MockMultipartFile image : imageFiles) {
            multipartRequest.file(image);
        }

        // when + then
        mockMvc.perform(multipartRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.BAD_REQUEST_ERROR.getStatus()));
    }

    @Test
    void createItem_shouldReturnBadRequest_whenImagePartMissing() throws Exception {
        // given
        ItemCreateRequest request = new ItemCreateRequest();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/v1/items/")
                        .file(requestPart)
                        .with(user(testUser)) // 시큐리티 설정 (AuthUser는 UserDetails 구현체여야 함)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ErrorCode.BAD_REQUEST_ERROR.getStatus())); // TODO: 핸들러 추가
    }
}