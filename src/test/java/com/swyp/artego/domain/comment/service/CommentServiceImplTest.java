package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentCreateResponse;
import com.swyp.artego.domain.comment.dto.response.CommentFindByItemIdResponse;
import com.swyp.artego.domain.comment.dto.response.CommentFindByItemIdWrapperResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("[댓글 조회] 로그인 하지 않은 사용자에겐 비밀 댓글이 보이지 않는다.")
    void getCommentsByItemId_unauthenticatedUser_cannotSeeSecretComments() {
        // given
        Comment rootComment = new Comment(user2, item1, "유저2의 첫댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        Comment replyComment = new Comment(creator1, item1, "작가의 대댓", true, rootComment);
        ReflectionTestUtils.setField(replyComment, "id", 2L);

        Comment replyComment2 = new Comment(user2, item1, "유저2의 대대댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 3L);

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findRootCommentsByItemId(eq(item1.getId()), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(rootComment)));
        given(commentRepository.findChildCommentsByParentIds(List.of(1L)))
                .willReturn(List.of(replyComment, replyComment2));

        // when
        CommentFindByItemIdWrapperResponse response = commentServiceImpl.getCommentsByItemId(null, item1.getId(), null, null);

        // then
        List<CommentFindByItemIdResponse> comments = response.getComments();

        // 루트 댓글과 replies 모두의 comment 내용이 "비밀 댓글입니다." 인지 확인
        for (CommentFindByItemIdResponse commentResponse : comments) {
            assertThat(commentResponse.getContent())
                    .isEqualTo("비밀 댓글입니다.");

            if (commentResponse.getReplies() != null) {
                for (CommentFindByItemIdResponse reply : commentResponse.getReplies()) {
                    assertThat(reply.getContent())
                            .isEqualTo("비밀 댓글입니다.");
                }
            }
        }
    }

    @Test
    @DisplayName("[댓글 조회] 작가는 비밀 댓글이 모두 보인다")
    void getCommentsByItemId_itemOwner_canSeeAllSecretComments() {
        // given
        Comment rootComment = new Comment(user2, item1, "유저2의 첫댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        Comment replyComment = new Comment(creator1, item1, "작가의 대댓", true, rootComment);
        ReflectionTestUtils.setField(replyComment, "id", 2L);

        Comment replyComment2 = new Comment(user2, item1, "유저2의 대대댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 3L);

        given(userRepository.findByOauthId(authCreator1.getOauthId())
        ).willReturn(Optional.of(creator1));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findRootCommentsByItemId(eq(item1.getId()), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(rootComment)));
        given(commentRepository.findChildCommentsByParentIds(List.of(1L)))
                .willReturn(List.of(replyComment, replyComment2));

        // when
        CommentFindByItemIdWrapperResponse response = commentServiceImpl.getCommentsByItemId(authCreator1, item1.getId(), null, null);

        // then
        List<CommentFindByItemIdResponse> comments = response.getComments();

        // 루트 댓글과 replies 모두의 comment 내용이 "비밀 댓글입니다." 이 아닌지 확인
        for (CommentFindByItemIdResponse commentResponse : comments) {
            assertThat(commentResponse.getContent())
                    .isNotEqualTo("비밀 댓글입니다.");

            if (commentResponse.getReplies() != null) {
                for (CommentFindByItemIdResponse reply : commentResponse.getReplies()) {
                    assertThat(reply.getContent())
                            .isNotEqualTo("비밀 댓글입니다.");
                }
            }
        }
    }

    @Test
    @DisplayName("[댓글 조회] 대화에 참여하고 있는 사용자는 해당 대화의 비밀 댓글이 모두 보인다.")
    void getCommentsByItemId_commentWriter_canSeeSecretCommentThreads() {
        // given
        Comment rootComment = new Comment(user2, item1, "유저2의 첫댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        Comment replyComment = new Comment(creator1, item1, "작가의 대댓", true, rootComment);
        ReflectionTestUtils.setField(replyComment, "id", 2L);

        Comment replyComment2 = new Comment(user2, item1, "유저2의 대대댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 3L);

        given(userRepository.findByOauthId(authUser2.getOauthId())
        ).willReturn(Optional.of(user2));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findRootCommentsByItemId(eq(item1.getId()), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(rootComment)));
        given(commentRepository.findChildCommentsByParentIds(List.of(1L)))
                .willReturn(List.of(replyComment, replyComment2));

        // when
        CommentFindByItemIdWrapperResponse response = commentServiceImpl.getCommentsByItemId(authUser2, item1.getId(), null, null);

        // then
        List<CommentFindByItemIdResponse> comments = response.getComments();

        // 루트 댓글과 replies 모두의 comment 내용이 "비밀 댓글입니다." 이 아닌지 확인
        for (CommentFindByItemIdResponse commentResponse : comments) {
            assertThat(commentResponse.getContent())
                    .isNotEqualTo("비밀 댓글입니다.");

            if (commentResponse.getReplies() != null) {
                for (CommentFindByItemIdResponse reply : commentResponse.getReplies()) {
                    assertThat(reply.getContent())
                            .isNotEqualTo("비밀 댓글입니다.");
                }
            }
        }
    }

    @Test
    @DisplayName("[댓글 조회] 대화에 참여하지 않은 사용자는 해당 대화의 비밀 댓글이 보이지 않는다.")
    void getCommentsByItemId_commentWriter_cannotSeeSecretCommentThreads() {
        // given
        AuthUser authUser3 = createAuthUser();
        User user3 = User.builder().oauthId(authUser3.getOauthId()).build();
        ReflectionTestUtils.setField(user3, "id", 3L);

        Comment rootComment = new Comment(user2, item1, "유저2의 첫댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        Comment replyComment = new Comment(creator1, item1, "작가의 대댓", true, rootComment);
        ReflectionTestUtils.setField(replyComment, "id", 2L);

        Comment replyComment2 = new Comment(user2, item1, "유저2의 대대댓", true, null);
        ReflectionTestUtils.setField(rootComment, "id", 3L);

        given(userRepository.findByOauthId(authUser3.getOauthId())
        ).willReturn(Optional.of(user3));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findRootCommentsByItemId(eq(item1.getId()), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(rootComment)));
        given(commentRepository.findChildCommentsByParentIds(List.of(1L)))
                .willReturn(List.of(replyComment, replyComment2));

        // when
        CommentFindByItemIdWrapperResponse response = commentServiceImpl.getCommentsByItemId(authUser3, item1.getId(), null, null);

        // then
        List<CommentFindByItemIdResponse> comments = response.getComments();

        // 루트 댓글과 replies 모두의 comment 내용이 "비밀 댓글입니다." 인지 확인
        for (CommentFindByItemIdResponse commentResponse : comments) {
            assertThat(commentResponse.getContent())
                    .isEqualTo("비밀 댓글입니다.");

            if (commentResponse.getReplies() != null) {
                for (CommentFindByItemIdResponse reply : commentResponse.getReplies()) {
                    assertThat(reply.getContent())
                            .isEqualTo("비밀 댓글입니다.");
                }
            }
        }
    }

    @Test
    @DisplayName("[대댓글 작성] 성공 - 사용자2 댓글에 대댓을 다는 크리에이터1")
    void createComment_createReply_shouldWorkSuccessfully_whenReplyCreatedByCreator() {
        // given
        Comment rootComment = new Comment(user2, item1, "", false, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        Comment replyComment = new Comment(creator1, item1, "대댓글 내용", false, rootComment);
        ReflectionTestUtils.setField(replyComment, "id", 2L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .itemId(item1.getId())
                .comment("대댓글 내용")
                .rootCommentId(rootComment.getId())
                .build();

        given(userRepository.findByOauthId(authCreator1.getOauthId())
        ).willReturn(Optional.of(creator1));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findById(rootComment.getId())
        ).willReturn(Optional.of(rootComment));

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

        Comment rootComment = new Comment(user2, item1, "", false, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .itemId(item1.getId())
                .comment("대댓글 내용")
                .rootCommentId(rootComment.getId())
                .build();

        given(userRepository.findByOauthId(anotherAuthUser3.getOauthId())
        ).willReturn(Optional.of(anotherUser3));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findById(rootComment.getId())
        ).willReturn(Optional.of(rootComment));

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
        Comment rootComment = new Comment(creator1, item1, "", false, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .itemId(item1.getId())
                .comment("대댓글 내용")
                .rootCommentId(rootComment.getId())
                .build();

        given(userRepository.findByOauthId(authUser2.getOauthId())
        ).willReturn(Optional.of(user2));

        given(itemRepository.findById(item1.getId())
        ).willReturn(Optional.of(item1));

        given(commentRepository.findById(rootComment.getId())
        ).willReturn(Optional.of(rootComment));

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
        Comment rootComment = new Comment(user2, item1, "", false, null);
        ReflectionTestUtils.setField(rootComment, "id", 1L);

        AuthUser anotherAuthUser3 = createAuthUser();
        User anotherUser3 = User.builder().oauthId(anotherAuthUser3.getOauthId()).build();
        ReflectionTestUtils.setField(anotherUser3, "id", 3L);

        given(userRepository.findByOauthId(anotherAuthUser3.getOauthId())
        ).willReturn(Optional.of(anotherUser3));

        given(commentRepository.findById(rootComment.getId())
        ).willReturn(Optional.of(rootComment));

        // when + then
        BusinessExceptionHandler exception = assertThrows(
                BusinessExceptionHandler.class,
                () -> commentServiceImpl.deleteComment(anotherAuthUser3, rootComment.getId())
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