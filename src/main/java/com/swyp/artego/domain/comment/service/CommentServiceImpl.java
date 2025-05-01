package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
import com.swyp.artego.domain.comment.dto.response.CommentReplyFindByItemIdResponse;
import com.swyp.artego.domain.comment.dto.response.CommentCreateResponse;
import com.swyp.artego.domain.comment.dto.response.CommentFindByItemIdResponse;
import com.swyp.artego.domain.comment.dto.response.CommentInfoResponse;
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

import java.util.*;
import java.util.stream.Collectors;

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

        // parentComment 가 존재하면 대댓글 작성 요청임
        Comment parentComment = commentRepository.findById(request.getParentCommentId()).orElse(null);
        if (parentComment != null) {
            validateReplyCommentPermission(user, parentComment, item);
        }

        return CommentCreateResponse.fromEntity(
            commentRepository.save(request.toEntity(user, item, parentComment))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentFindByItemIdResponse> getCommentsByItemId(Long itemId) {
        itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("존재하지 않는 작품입니다.", ErrorCode.NOT_FOUND_ERROR));

        List<Comment> flatList = commentRepository.findByItemIdOrderByCreatedAtDesc(itemId);
        
        return convertFlatToDepth1Tree(flatList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentInfoResponse> getAllComments() {
        return commentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CommentInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 대댓글 생성의 경우, 최초 댓글의 작성자와 작가 본인만 대댓글을 작성할 수 있다.
     * 두 경우가 아닌 사용자가 대댓글을 작성할 때 에러를 던진다.
     *
     * @param user 대댓글을 작성하는 유저
     * @param parentComment 대댓글이 작성되는 원댓글
     * @param item 대댓글이 작성되는 작품
     */
    private static void validateReplyCommentPermission(User user, Comment parentComment, Item item) {
        Long replyAuthorId = user.getId();
        Long parentCommentAuthorId = parentComment.getUser().getId();
        Long itemCreatorId = item.getUser().getId();

        if (!Objects.equals(parentCommentAuthorId, replyAuthorId) && !Objects.equals(itemCreatorId, replyAuthorId)) {
            throw new BusinessExceptionHandler("대댓글 작성 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }
    }

    /**
     * flat 구조의 댓글 목록을 depth 1의 계층형 댓글 응답 구조로 변환한다.
     *
     * parent가 null인 댓글을 루트 댓글로 간주하고,
     * 해당 댓글을 기준으로 직접적인 자식(대댓글)들을 묶어 replies 필드에 포함한다.
     * 대댓글은 루트 댓글 하나에만 속하며, 추가 중첩은 지원하지 않는다.
     *
     * @param comments 계층 구조 없이 정렬된 Comment 엔티티 리스트
     * @return List<CommentFindByItemIdResponse> 루트 댓글과 그에 속한 대댓글을 포함하는 응답 DTO 리스트
     */
    public List<CommentFindByItemIdResponse> convertFlatToDepth1Tree(List<Comment> comments) {
        Map<Long, List<Comment>> repliesGroupedByParentComment = new HashMap<>();
        List<Comment> parentComments = new ArrayList<>();

        // 댓글과 대댓글을 분류하여 부모-자식 관계로 그룹화
        for (Comment comment : comments) {
            if (comment.getParent() == null) {
                parentComments.add(comment);
            } else {
                repliesGroupedByParentComment
                        .computeIfAbsent(comment.getParent().getId(), key -> new ArrayList<>())
                        .add(comment);
            }
        }

        // 계층형 응답 DTO로 변환
        return parentComments.stream()
                .map(comment -> CommentFindByItemIdResponse.builder()
                        .id(comment.getId())
                        .comment(comment.getComment())
                        .writer(comment.getUser().getName())
                        .createdAt(comment.getCreatedAt())
                        .secret(comment.isSecret())
                        .reply(
                                Optional.ofNullable(repliesGroupedByParentComment.get(comment.getId()))
                                        .map(replyList -> replyList.stream()
                                                .map(reply -> CommentReplyFindByItemIdResponse.builder()
                                                        .comment(reply.getComment())
                                                        .writer(reply.getUser().getName())
                                                        .createdAt(reply.getCreatedAt())
                                                        .secret(reply.isSecret())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .orElse(null)
                        )
                        .build())
                .collect(Collectors.toList());
    }
}
