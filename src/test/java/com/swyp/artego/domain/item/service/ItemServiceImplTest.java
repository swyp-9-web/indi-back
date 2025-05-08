package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.dto.response.NaverOAuth2Response;
import com.swyp.artego.global.auth.oauth.dto.response.OAuth2Response;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemServiceImpl;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private AuthUser authCreator1;
    private User creator1;

    private Item item1;

    @BeforeEach
    void setUp() {
        authCreator1 = createAuthUser();
        creator1 = User.builder().oauthId(authCreator1.getOauthId()).build();
        ReflectionTestUtils.setField(creator1, "id", 1L);

        item1 = Item.builder().user(creator1).title("title").build();
        ReflectionTestUtils.setField(item1, "id", 1L);
    }

    @Test
    @DisplayName("[작품 삭제] 예외 발생 - 다른 사용자가 작품 삭제를 시도")
    void deleteItem_shouldThrowBusinessException_whenInvalidOwnerRequest() {
        // given
        AuthUser anotherAuthUser2 = createAuthUser();
        User anotherUser2 = User.builder().oauthId(anotherAuthUser2.getOauthId()).build();
        ReflectionTestUtils.setField(anotherUser2, "id", 2L);

        given(userRepository.findByOauthId(anotherAuthUser2.getOauthId())
        ).willReturn(Optional.of(anotherUser2));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        // when + then
        BusinessExceptionHandler exception = assertThrows(
                BusinessExceptionHandler.class,
                () -> itemServiceImpl.deleteItem(anotherAuthUser2, item1.getId())
        );

        assertEquals(ErrorCode.FORBIDDEN_ERROR, exception.getErrorCode());
        verify(itemRepository, never()).save(any(Item.class));
    }

    private static AuthUser createAuthUser() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "1234567890");
        userInfo.put("email", "testuser@naver.com");
        userInfo.put("name", "Test User");
        userInfo.put("mobile", "010-1234-5678");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", userInfo);

        OAuth2Response naverResponse = new NaverOAuth2Response(attributes);
        return new AuthUser(naverResponse, "ROLE");
    }
}