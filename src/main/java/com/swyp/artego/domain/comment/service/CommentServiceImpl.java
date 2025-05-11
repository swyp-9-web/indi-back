package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.request.CommentUpdateRequest;
import com.swyp.artego.domain.comment.dto.response.*;
import com.swyp.artego.domain.comment.entity.Comment;
import com.swyp.artego.domain.comment.repository.CommentRepository;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public CommentCreateResponse createComment(AuthUser authUser, CommentCreateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Comment rootComment = null;
        if (request.getRootCommentId() != null) {
            rootComment = commentRepository.findById(request.getRootCommentId())
                    .orElseThrow(() -> new BusinessExceptionHandler("존재하지 않는 parentId 입니다.", ErrorCode.NOT_FOUND_ERROR));
            if (rootComment.getParent() != null) {
                throw new BusinessExceptionHandler("루트 댓글의 id가 아닙니다.", ErrorCode.NOT_VALID_ERROR);
            }

            validateReplyCommentPermission(user, rootComment, item);
        }

        return CommentCreateResponse.fromEntity(
                commentRepository.save(request.toEntity(user, item, rootComment))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CommentFindByItemIdWrapperResponse getCommentsByItemId(AuthUser authUser, Long itemId) {
        Long viewerId = null;
        if (authUser != null) {
            User user = userRepository.findByOauthId(authUser.getOauthId())
                    .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

            viewerId = user.getId();
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("존재하지 않는 작품입니다.", ErrorCode.NOT_FOUND_ERROR));
        long itemOwnerId = item.getUser().getId();

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedAtDesc(itemId);

        applySecretPolicy(comments, viewerId, itemOwnerId);

        return CommentFindByItemIdWrapperResponse.from(item.getUser(), comments);
    }


    @Override
    @Transactional(readOnly = true)
    public MyCommentActivityResultResponse getMyCommentActivities(AuthUser authUser, int page, int limit) {
        return commentRepository.findMyCommentActivity(authUser, page, limit);
    }


    @Override
    @Transactional
    public CommentUpdateResponse updateComment(AuthUser authUser, Long commentId, CommentUpdateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessExceptionHandler("존재하지 않는 댓글입니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BusinessExceptionHandler("댓글을 수정할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        if (request.getComment() != null) {
            comment.setComment(request.getComment());
        }

        if (request.getSecret() != null) {
            comment.setSecret(request.getSecret());
        }

        return CommentUpdateResponse.fromEntity(comment);
    }

    @Override
    @Transactional
    public CommentDeleteResponse deleteComment(AuthUser authUser, Long commentId) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessExceptionHandler("존재하지 않는 댓글입니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BusinessExceptionHandler("댓글을 삭제할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        // commentId를 parentId로 갖는 댓글이 있으면 실제 삭제 불가능.
        if (commentRepository.findByParentId(commentId) == null) {
            commentRepository.delete(comment);
        } else {
            comment.setDeleted(true);
            comment.setComment("삭제된 댓글입니다.");
        }

        return CommentDeleteResponse.from(commentId);
    }

    /**
     * 대댓글 생성의 경우, 최초 댓글의 작성자와 작가 본인만 대댓글을 작성할 수 있다.
     * 두 경우가 아닌 사용자가 대댓글을 작성할 때 에러를 던진다.
     *
     * @param user        대댓글을 작성하는 유저
     * @param rootComment 대댓글이 작성되는 원댓글
     * @param item        대댓글이 작성되는 작품
     */
    private static void validateReplyCommentPermission(User user, Comment rootComment, Item item) {
        Long replyAuthorId = user.getId();
        Long rootCommentAuthorId = rootComment.getUser().getId();
        Long itemCreatorId = item.getUser().getId();

        if (!Objects.equals(rootCommentAuthorId, replyAuthorId) && !Objects.equals(itemCreatorId, replyAuthorId)) {
            throw new BusinessExceptionHandler("대댓글 작성 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }
    }

    /**
     * 비밀 댓글인 경우 아래 정책에 따라 댓글을 가린다.
     * <p>
     * 루트 댓글의 작성자는 해당 스레드를 모두 열람할 수 있다.
     * 작가는 모든 댓글을 열람할 수 있다.
     * 로그인한 제 3자나 로그인하지 않은 유저는 비밀 댓글을 볼 수 없다.
     *
     * @param comments
     * @param viewerId    댓글 조회 요청을 한 사람의 id. 로그인하지 않은 사용자는 null.
     * @param itemOwnerId 작가 id
     */
    private void applySecretPolicy(List<Comment> comments, Long viewerId, long itemOwnerId) {
        Long currentRootCommmentWriterId = null;

        for (Comment comment : comments) {
            if (comment.getParent() == null) {
                // 루트 댓글 진입 시 현재 스레드 기준 변경
                currentRootCommmentWriterId = comment.getUser().getId();
            }

            if (!comment.isSecret()) {
                // 공개 댓글은 항상 보여진다.
                continue;
            }

            boolean isItemOwner
                    = viewerId != null && viewerId.equals(itemOwnerId);
            boolean isRootCommentWriter
                    = viewerId != null && viewerId.equals(currentRootCommmentWriterId);

            if (!(isItemOwner || isRootCommentWriter)) {
                comment.setComment("비밀 댓글입니다.");
            }
        }
    }
}
