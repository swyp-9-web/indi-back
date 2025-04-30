package com.swyp.artego.domain.comment.service;

import com.swyp.artego.domain.comment.dto.request.CommentCreateRequest;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void createComment(AuthUser authUser, CommentCreateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessExceptionHandler("아이템이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Comment comment = Comment.builder()
                .user(user)
                .item(item)
                .comment(request.getComment())
                .secret(request.isSecret())
                .build();

        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentInfoResponse> getAllComments() {
        return commentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CommentInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
