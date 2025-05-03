package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentCreateResponse;
import com.swyp.artego.domain.comment.entity.Comment;
import com.swyp.artego.domain.comment.repository.CommentRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentServiceImpl;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private AuthUser authCreator1;
    private User creator1;

    private AuthUser authUser2;
    private User user2;

    private Item item1;

    @BeforeEach
    void setUp() {
        authCreator1 = createAuthUser();
        creator1 = User.builder().oauthId(authCreator1.getOauthId()).build();
        ReflectionTestUtils.setField(creator1, "id", 1L);

        authUser2 = createAuthUser();
        user2 = User.builder().oauthId(authUser2.getOauthId()).build();
        ReflectionTestUtils.setField(user2, "id", 2L);

        item1 = Item.builder().user(creator1).title("title").build();
        ReflectionTestUtils.setField(item1, "id", 1L);
    }

    @Test
    @DisplayName("[대댓글 작성] 성공 - 사용자2 댓글에 대댓을 다는 크리에이터1")
    void createComment_createReply_shouldWorkSuccessfully_whenReplyCreatedByCreator() {
        // given
        Comment parentComment = new Comment(user2, item1, "", false, null);
        ReflectionTestUtils.setField(parentComment, "id", 1L);

        Comment replyComment = new Comment(creator1, item1, "대댓글 내용", false, parentComment);
        ReflectionTestUtils.setField(replyComment, "id", 2L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .itemId(item1.getId())
                .comment("대댓글 내용")
                .parentCommentId(parentComment.getId())
                .build();

        given(userRepository.findByOauthId(authCreator1.getOauthId())
        ).willReturn(Optional.of(creator1));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findById(parentComment.getId())
        ).willReturn(Optional.of(parentComment));

        given(commentRepository.save(any(Comment.class)))
                .willReturn(replyComment);

        // when
        CommentCreateResponse response = commentServiceImpl.createComment(authCreator1, request);

        // then
        assertEquals(2L, response.getCommentId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("[대댓글 작성] 예외 발생 - 사용자2 댓글에 대댓을 다는 사용자3")
    void createComment_createReply_shouldThrowBusinessException_whenReplyCreatedByNonOriginalCommenterOrNonCreator() {
        // given
        AuthUser anotherAuthUser3 = createAuthUser();
        User anotherUser3 = User.builder().oauthId(anotherAuthUser3.getOauthId()).build();
        ReflectionTestUtils.setField(anotherUser3, "id", 3L);

        Comment parentComment = new Comment(user2, item1, "", false, null);
        ReflectionTestUtils.setField(parentComment, "id", 1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .itemId(item1.getId())
                .comment("대댓글 내용")
                .parentCommentId(parentComment.getId())
                .build();

        given(userRepository.findByOauthId(anotherAuthUser3.getOauthId())
        ).willReturn(Optional.of(anotherUser3));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findById(parentComment.getId())
        ).willReturn(Optional.of(parentComment));

        // when + then
        BusinessExceptionHandler exception = assertThrows(
                BusinessExceptionHandler.class,
                () -> commentServiceImpl.createComment(anotherAuthUser3, request)
        );

        assertEquals(ErrorCode.FORBIDDEN_ERROR, exception.getErrorCode());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("[대댓글 작성] 예외 발생 - 크리에이터1 댓글에 대댓을 다는 사용자2")
    void createComment_createReply_shouldThrowBusinessException_whenReplyCreatedByNonCreator() {
        // given
        Comment parentComment = new Comment(creator1, item1, "", false, null);
        ReflectionTestUtils.setField(parentComment, "id", 1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .itemId(item1.getId())
                .comment("대댓글 내용")
                .parentCommentId(parentComment.getId())
                .build();

        given(userRepository.findByOauthId(authUser2.getOauthId())
        ).willReturn(Optional.of(user2));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findById(parentComment.getId())
        ).willReturn(Optional.of(parentComment));

        // when + then
        BusinessExceptionHandler exception = assertThrows(
                BusinessExceptionHandler.class,
                () -> commentServiceImpl.createComment(authUser2, request)
        );

        assertEquals(ErrorCode.FORBIDDEN_ERROR, exception.getErrorCode());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("[댓글 삭제] 예외 발생 - 다른 사용자가 댓글을 삭제 시도")
    void deleteComment_shouldThrowBusinessException_whenInvalidUserRequest() {
        Comment parentComment = new Comment(user2, item1, "", false, null);
        ReflectionTestUtils.setField(parentComment, "id", 1L);

        AuthUser anotherAuthUser3 = createAuthUser();
        User anotherUser3 = User.builder().oauthId(anotherAuthUser3.getOauthId()).build();
        ReflectionTestUtils.setField(anotherUser3, "id", 3L);

        given(userRepository.findByOauthId(anotherAuthUser3.getOauthId())
        ).willReturn(Optional.of(anotherUser3));

        given(commentRepository.findById(parentComment.getId())
        ).willReturn(Optional.of(parentComment));

        // when + then
        BusinessExceptionHandler exception = assertThrows(
                BusinessExceptionHandler.class,
                () -> commentServiceImpl.deleteComment(anotherAuthUser3, parentComment.getId())
        );

        assertEquals(ErrorCode.FORBIDDEN_ERROR, exception.getErrorCode());
        verify(commentRepository, never()).save(any(Comment.class));
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