package com.swyp.artego.domain.comment.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.artego.domain.comment.dto.response.MyCommentActivityResponse;
import com.swyp.artego.domain.comment.dto.response.MyCommentActivityResultResponse;
import com.swyp.artego.domain.comment.entity.QComment;
import com.swyp.artego.domain.item.dto.response.MetaResponse;
import com.swyp.artego.domain.item.entity.QItem;
import com.swyp.artego.domain.user.entity.QUser;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
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

        // 1. 페이징 계산
        int page = Math.max(pageInput - 1, 0);
        int limit = limitInput > 0 ? limitInput : 10;
        Pageable pageable = PageRequest.of(page, limit);
        long offset = pageable.getOffset();

        // 2. 내가 단 댓글 중 작품별 최신 댓글 ID만 추출 (일반 댓글 + 대댓글 모두 포함)
        List<Long> latestCommentIds = queryFactory
                .select(comment.id)
                .from(comment)
                .where(
                        comment.user.id.eq(userId),
                        comment.deleted.isFalse(),
                        comment.createdAt.eq(
                                JPAExpressions
                                        .select(sub.createdAt.max())
                                        .from(sub)
                                        .where(
                                                sub.item.id.eq(comment.item.id),
                                                sub.user.id.eq(userId),
                                                sub.deleted.isFalse()
                                        )
                        )
                )
                .orderBy(comment.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();

        Long totalCount = queryFactory
                .select(comment.item.id.countDistinct())
                .from(comment)
                .where(
                        comment.user.id.eq(userId),
                        comment.deleted.isFalse()
                )
                .fetchOne();

        // 3. 실제 데이터 조회
        List<MyCommentActivityResponse> items = queryFactory
                .select(Projections.constructor(MyCommentActivityResponse.class,
                        item.id,
                        item.title,
                        item.price,
                        item.imgUrls,
                        artist.nickname,

                        Projections.constructor(MyCommentActivityResponse.CommentDto.class,
                                comment.id,
                                comment.comment,
                                comment.createdAt,
                                commenter.nickname,
                                commenter.imgUrl
                        ),

                        Projections.constructor(MyCommentActivityResponse.CommentDto.class,
                                reply.id,
                                reply.comment,
                                reply.createdAt,
                                new CaseBuilder()
                                        .when(reply.id.isNotNull()).then(artist.nickname)
                                        .otherwise((String) null),
                                new CaseBuilder()
                                        .when(reply.id.isNotNull()).then(artist.imgUrl)
                                        .otherwise((String) null)
                        )
                ))
                .from(comment)
                .join(comment.item, item)
                .join(item.user, artist)
                .join(comment.user, commenter)
                .leftJoin(reply)
                .on(reply.parent.eq(comment)
                        .and(reply.user.eq(artist))
                        .and(reply.deleted.isFalse()))
                .where(comment.id.in(latestCommentIds))
                .orderBy(comment.createdAt.desc())
                .fetch();

        // 4. Meta 정보
        MetaResponse meta = MetaResponse.builder()
                .currentPage(page + 1)
                .pageSize(limit)
                .totalItems(totalCount == null ? 0 : totalCount)
                .hasNextPage((page + 1) * limit < (totalCount == null ? 0 : totalCount))
                .build();

        // 5. 응답 조립
        return MyCommentActivityResultResponse.builder()
                .items(items)
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
