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

    @Override
    public MyCommentActivityResultResponse findMyCommentActivity(AuthUser authUser, int pageInput, int limitInput) {
        // QueryDSL용 엔티티 alias 설정
        QComment comment = QComment.comment1;
        QComment sub = new QComment("sub"); // 서브쿼리용 댓글
        QComment reply = new QComment("reply"); // 답글
        QItem item = QItem.item;
        QUser creator = QUser.user; // 작가 (아이템 만든 사람)
        QUser commenter = new QUser("commenter"); // 댓글 작성자
        QUser responder = new QUser("responder"); // 답글 작성자

        // 로그인 확인
        if (authUser == null) {
            throw new IllegalArgumentException("로그인한 유저만 사용 가능합니다.");
        }

        // 로그인한 유저 정보 조회
        User loginUser = getLoginUser(authUser);
        Long userId = loginUser.getId();

        // 페이징 처리
        int page = Math.max(pageInput - 1, 0); // 1페이지부터 시작하도록
        int limit = limitInput > 0 ? limitInput : 10;
        Pageable pageable = PageRequest.of(page, limit);
        long offset = pageable.getOffset();

        // 1. 내가 단 댓글 중 작품별 가장 최신 댓글 ID만 추출
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

        // 2. 전체 댓글 수 (작품 기준 중복 제거)
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
                        item.imgUrls, // 아이템 이미지들

                        creator.id,
                        creator.nickname,
                        creator.imgUrl, // 작가 정보

                        comment.id,
                        comment.comment,
                        comment.createdAt, // 내 댓글 정보

                        commenter.id,
                        commenter.nickname,
                        commenter.imgUrl, // 댓글 단 유저 (나)

                        reply.id,
                        reply.comment,
                        reply.createdAt, // 답글 정보

                        responder.id,
                        responder.nickname,
                        responder.imgUrl, // 답글 단 유저

                        JPAExpressions.select(QComment.comment1.count())
                                .from(QComment.comment1)
                                .where(QComment.comment1.item.id.eq(item.id)) // 해당 아이템의 총 댓글 수
                )
                .from(comment)
                .join(comment.item, item) // 댓글 → 아이템 조인
                .join(item.user, creator) // 아이템 → 작가 조인
                .join(comment.user, commenter) // 댓글 → 작성자 조인
                .leftJoin(reply).on(
                        reply.id.eq(
                                JPAExpressions
                                        .select(reply.id)
                                        .from(reply)
                                        .where(
                                                reply.parent.id.eq(comment.parent.id.coalesce(comment.id)), // 같은 댓글 스레드 내
                                                reply.user.id.ne(userId), // 내가 아닌 사람의 답글
                                                reply.createdAt.gt(comment.createdAt) // 내 댓글보다 늦게 달린 답글
                                        )
                                        .orderBy(reply.createdAt.asc()) // 가장 빠른 답글 1개
                                        .limit(1)
                        )
                )
                .leftJoin(reply.user, responder) // 답글 → 작성자 조인
                .where(comment.id.in(latestCommentIds)) // 위에서 추출한 최신 댓글 ID만 대상
                .orderBy(comment.createdAt.desc()) // 최신 순 정렬
                .fetch();

        // 4. Tuple → DTO 변환
        List<MyCommentActivityResponse> responses = tuples.stream().map(t -> {
            List<String> imgUrls = t.get(item.imgUrls);
            String thumbnail = (imgUrls != null && !imgUrls.isEmpty()) ? imgUrls.get(0) : null;

            // 작가 정보 DTO
            MyCommentActivityResponse.ItemDTO.ArtistDTO creatorDTO = MyCommentActivityResponse.ItemDTO.ArtistDTO.builder()
                    .id(t.get(creator.id))
                    .nickname(t.get(creator.nickname))
                    .build();

            Long commentCount = t.get(19, Long.class); // 댓글 총 개수

            // 아이템 정보 DTO
            MyCommentActivityResponse.ItemDTO itemDTO = MyCommentActivityResponse.ItemDTO.builder()
                    .id(t.get(item.id))
                    .title(t.get(item.title))
                    .price(t.get(item.price))
                    .artist(creatorDTO)
                    .thumbnailImgUrl(thumbnail)
                    .commentCount(commentCount)
                    .build();

            // 내 댓글 작성자 정보
            MyCommentActivityResponse.UserDTO myUser = MyCommentActivityResponse.UserDTO.builder()
                    .id(t.get(commenter.id))
                    .nickname(t.get(commenter.nickname))
                    .profileImgUrl(t.get(commenter.imgUrl))
                    .build();

            // 내 댓글 DTO
            MyCommentActivityResponse.CommentDTO myComment = MyCommentActivityResponse.CommentDTO.builder()
                    .id(t.get(comment.id))
                    .content(t.get(comment.comment))
                    .createdAt(t.get(comment.createdAt))
                    .user(myUser)
                    .build();

            // 답글 DTO (존재할 경우에만)
            MyCommentActivityResponse.CommentDTO replyComment = null;
            if (t.get(reply.id) != null) {
                MyCommentActivityResponse.UserDTO responderDTO = MyCommentActivityResponse.UserDTO.builder()
                        .id(t.get(responder.id))
                        .nickname(t.get(responder.nickname))
                        .profileImgUrl(t.get(responder.imgUrl))
                        .build();

                replyComment = MyCommentActivityResponse.CommentDTO.builder()
                        .id(t.get(reply.id))
                        .content(t.get(reply.comment))
                        .createdAt(t.get(reply.createdAt))
                        .user(responderDTO)
                        .build();
            }

            // 최종 응답 조립
            return MyCommentActivityResponse.builder()
                    .item(itemDTO)
                    .myComment(myComment)
                    .replyComment(replyComment)
                    .build();
        }).toList();

        // 페이징 메타 정보 생성
        MetaResponse meta = MetaResponse.builder()
                .currentPage(page + 1)
                .pageSize(limit)
                .totalItems(totalCount == null ? 0 : totalCount)
                .hasNextPage((page + 1) * limit < (totalCount == null ? 0 : totalCount))
                .build();

        // 최종 응답 객체 반환
        return MyCommentActivityResultResponse.builder()
                .comments(responses)
                .meta(meta)
                .build();
    }

    // 로그인한 유저 정보를 DB에서 조회
    private User getLoginUser(AuthUser authUser) {
        if (authUser == null) return null;
        return userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid auth user"));
    }
}
