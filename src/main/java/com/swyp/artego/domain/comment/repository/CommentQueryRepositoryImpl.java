package com.swyp.artego.domain.comment.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.artego.domain.comment.dto.response.MyCommentActivityResponse;
import com.swyp.artego.domain.comment.dto.response.MyCommentActivityResultResponse;
import com.swyp.artego.domain.comment.entity.QComment;
import com.swyp.artego.domain.item.entity.QItem;
import com.swyp.artego.domain.user.entity.QUser;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.dto.response.MetaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final UserRepository userRepository;

    private final CommentRepository commentRepository; //

    @Override
    public MyCommentActivityResultResponse findMyCommentActivity(AuthUser authUser, int pageInput, int limitInput) {
        QComment comment = QComment.comment1;
        QComment sub = new QComment("sub");
        QComment reply = new QComment("reply");
        QItem item = QItem.item;
        QUser artist = QUser.user;
        QUser commenter = new QUser("commenter");

        if (authUser == null) {
            throw new IllegalArgumentException("로그인한 유저만 사용 가능합니다.");
        }

        User loginUser = getLoginUser(authUser);
        Long userId = loginUser.getId();

        int page = Math.max(pageInput - 1, 0);
        int limit = limitInput > 0 ? limitInput : 10;
        Pageable pageable = PageRequest.of(page, limit);
        long offset = pageable.getOffset();

        // 1. 내가 단 댓글 중 작품별 최신 댓글 ID만 추출
        List<Long> latestCommentIds = queryFactory
                .select(comment.id)
                .from(comment)
                .where(
                        comment.user.id.eq(userId),
                        comment.createdAt.eq(
                                JPAExpressions
                                        .select(sub.createdAt.max())
                                        .from(sub)
                                        .where(
                                                sub.item.id.eq(comment.item.id),
                                                sub.user.id.eq(userId)
                                        )
                        )
                )
                .orderBy(comment.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();

        // 2. 총 개수
        Long totalCount = queryFactory
                .select(comment.item.id.countDistinct())
                .from(comment)
                .where(comment.user.id.eq(userId))
                .fetchOne();

        // 3. 실제 데이터 조회
        List<Tuple> tuples = queryFactory
                .select(
                        item.id,
                        item.title,
                        item.price,
                        item.imgUrls,

                        artist.id,
                        artist.nickname,
                        artist.imgUrl,

                        comment.id,
                        comment.comment,
                        comment.createdAt,

                        commenter.id,
                        commenter.nickname,
                        commenter.imgUrl,

                        reply.id,
                        reply.comment,
                        reply.createdAt
                )
                .from(comment)
                .join(comment.item, item)
                .join(item.user, artist)
                .join(comment.user, commenter)
                .leftJoin(reply).on(
                        reply.id.eq(
                                JPAExpressions
                                        .select(reply.id)
                                        .from(reply)
                                        .where(
                                                reply.parent.id.eq(comment.parent.id.coalesce(comment.id)),
                                                reply.user.id.ne(userId),
                                                reply.createdAt.gt(comment.createdAt)
                                        )
                                        .orderBy(reply.createdAt.asc())
                                        .limit(1)
                        )
                )
                .where(comment.id.in(latestCommentIds))
                .orderBy(comment.createdAt.desc())
                .fetch();

        List<MyCommentActivityResponse> responses = tuples.stream().map(t -> {
            List<String> imgUrls = t.get(item.imgUrls);
            String thumbnail = (imgUrls != null && !imgUrls.isEmpty()) ? imgUrls.get(0) : null;

            MyCommentActivityResponse.ItemDTO.ArtistDTO artistDTO = MyCommentActivityResponse.ItemDTO.ArtistDTO.builder()
                    .id(t.get(artist.id))
                    .nickname(t.get(artist.nickname))
                    .build();

            MyCommentActivityResponse.ItemDTO itemDTO = MyCommentActivityResponse.ItemDTO.builder()
                    .id(t.get(item.id))
                    .title(t.get(item.title))
                    .price(t.get(item.price))
                    .artist(artistDTO)
                    .thumbnailImgUrl(thumbnail)
                    .build();

            MyCommentActivityResponse.UserDTO myUser = MyCommentActivityResponse.UserDTO.builder()
                    .id(t.get(commenter.id))
                    .nickname(t.get(commenter.nickname))
                    .profileImgUrl(t.get(commenter.imgUrl))
                    .build();

            MyCommentActivityResponse.CommentDTO myComment = MyCommentActivityResponse.CommentDTO.builder()
                    .id(t.get(comment.id))
                    .content(t.get(comment.comment))
                    .createdAt(t.get(comment.createdAt))
                    .user(myUser)
                    .build();

            MyCommentActivityResponse.CommentDTO replyComment = null;
            if (t.get(reply.id) != null) {
                MyCommentActivityResponse.UserDTO replyUser = MyCommentActivityResponse.UserDTO.builder()
                        .id(t.get(artist.id)) // 상대 유저 정보를 여기 artist 변수로 가져옴
                        .nickname(t.get(artist.nickname))
                        .profileImgUrl(t.get(artist.imgUrl))
                        .build();

                replyComment = MyCommentActivityResponse.CommentDTO.builder()
                        .id(t.get(reply.id))
                        .content(t.get(reply.comment))
                        .createdAt(t.get(reply.createdAt))
                        .user(replyUser)
                        .build();
            }

            return MyCommentActivityResponse.builder()
                    .item(itemDTO)
                    .myComment(myComment)
                    .replyComment(replyComment)
                    .build();
        }).toList();

        MetaResponse meta = MetaResponse.builder()
                .currentPage(page + 1)
                .pageSize(limit)
                .totalItems(totalCount == null ? 0 : totalCount)
                .hasNextPage((page + 1) * limit < (totalCount == null ? 0 : totalCount))
                .build();

        return MyCommentActivityResultResponse.builder()
                .comments(responses)
                .meta(meta)
                .build();
    }



    // 로그인 유저 조회
    private User getLoginUser(AuthUser authUser) {
        if (authUser == null) return null;
        return userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid auth user"));
    }


}
