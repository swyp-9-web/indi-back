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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

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

    private final String URL_PREFIX = "/api/v1/items";

    /**
     * 테스트 환경에서 javax.validation.Validator를 수동으로 빈 등록하기 위한 설정 클래스
     * <p>
     * Multipart/form-data와 JSON을 함께 사용하는 경우,
     *
     * @RequestPart로 전달된 DTO에 @Valid가 자동 적용되지 않는 이슈가 발생할 수 있으므로,
     * Validator를 직접 주입 받아 수동 검증을 수행하기 위해 사용한다.
     */
    @TestConfiguration
    static class ValidatorConfig {
        @Bean
        public Validator validator() {
            return Validation.buildDefaultValidatorFactory().getValidator();
        }
    }

    /**
     * [테스트 실행 전] 테스트용 AuthUser를 생성합니다.
     */
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

    /**
     * [단위 테스트] createItem 메서드 - 유효한 요청 DTO로 아이템 생성 시 성공 응답을 검증한다.
     */
    @Test
    void createItem_shouldWorkSuccessfully_whenValidRequestDtoProvided() throws Exception {
        // given
        ItemCreateRequest request = ItemCreateRequest.builder()
                .title("title")
                .categoryType(CategoryType.TEXTILE_ART)
                .size(new ItemCreateRequest.ItemSize(10, 20, 0))
                .material("material")
                .description("description")
                .price(10000)
                .statusType(StatusType.OPEN)
                .build();

        ItemCreateResponse mockResponse = new ItemCreateResponse(1L); // 응답 객체 생성

        when(itemService.createItem(
                Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(mockResponse);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "images", "image1.jpg", "image/jpeg", "dummy image".getBytes()
        );

        // when + then
        mockMvc.perform(multipart("/api/v1/items")
                        .file(requestPart)
                        .file(imagePart)
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SuccessCode.INSERT_SUCCESS.getStatus()));
    }

    /**
     * [예외 테스트] createItem - 이미지 파일이 최대 개수 초과 시 400 Bad Request를 반환하는지 검증
     *
     * @throws Exception
     */
    @Test
    void createItem_shouldReturnBadRequest_whenTooManyImagesProvided() throws Exception {
        // given
        ItemCreateRequest request = ItemCreateRequest.builder()
                .size(new ItemCreateRequest.ItemSize(0, 20, 30))
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartHttpServletRequestBuilder multipartRequest = (MockMultipartHttpServletRequestBuilder) multipart(URL_PREFIX)
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

    /**
     * [예외 테스트] createItem - 이미지 파트가 누락되었을 때 400 Bad Request 반환 여부 검증
     *
     * @throws Exception
     */
    @Test
    void createItem_shouldReturnBadRequest_whenImagePartMissing() throws Exception {
        // given
        ItemCreateRequest request = ItemCreateRequest.builder()
                .size(new ItemCreateRequest.ItemSize(0, 20, 30))
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart(URL_PREFIX)
                        .file(requestPart)
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.BAD_REQUEST_ERROR.getStatus()));
    }

    /**
     * [예외 테스트] createItem - 유효하지 않은 사이즈(ItemSize)가 주어진 경우 400 Bad Request 반환 여부 검증
     * <p>
     * 잘못된 사이즈 조합 예:
     * - (0, 0, 30)
     * - (10, 0, 30)
     * - (10, -1, 30)
     * - (10, 0, 0)
     *
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource("invalidSizeProvider")
    void createItem_shouldReturnNotValidError_whenSizeIsWrong(ItemCreateRequest.ItemSize invalidSize) throws Exception {
        // given
        ItemCreateRequest request = ItemCreateRequest.builder()
                .title("title")
                .categoryType(CategoryType.TEXTILE_ART)
                .size(invalidSize)
                .material("material")
                .description("description")
                .price(10000)
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "images", "image1.jpg", "image/jpeg", "dummy image".getBytes()
        );

        // when + then
        mockMvc.perform(multipart(URL_PREFIX)
                        .file(requestPart)
                        .file(imagePart)
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.divisionCode").value(ErrorCode.NOT_VALID_ERROR.getDivisionCode()));
    }

    private static Stream<ItemCreateRequest.ItemSize> invalidSizeProvider() {
        return Stream.of(
                new ItemCreateRequest.ItemSize(0, 0, 30),
                new ItemCreateRequest.ItemSize(10, 0, 30),
                new ItemCreateRequest.ItemSize(10, -1, 30),
                new ItemCreateRequest.ItemSize(10, 0, 0)
        );
    }
}