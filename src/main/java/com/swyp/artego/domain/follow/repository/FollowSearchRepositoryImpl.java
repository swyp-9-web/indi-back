package com.swyp.artego.domain.follow.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.artego.domain.follow.dto.response.FollowedArtistResponse;
import com.swyp.artego.domain.follow.dto.response.FollowedArtistsResponse;
import com.swyp.artego.domain.follow.entity.QFollow;
import com.swyp.artego.domain.item.dto.response.ItemSearchResponse;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.entity.QItem;
import com.swyp.artego.domain.scrap.entity.QScrap;
import com.swyp.artego.domain.user.entity.QUser;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.dto.response.MetaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FollowSearchRepositoryImpl implements FollowSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public FollowedArtistsResponse findFollowedArtistsWithItems(Long userId, Integer page, Integer size) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인한 유저만 사용 가능합니다.");
        }

        QFollow follow = QFollow.follow;
        QUser artist = new QUser("artist");
        QItem item = QItem.item;
        QScrap scrap = QScrap.scrap;

        // 페이지 번호가 0부터 시작하므로 page-1을 적용, 음수가 되지 않도록 보정
        int safePage = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(safePage, size);

        // 1. 해당 유저가 팔로우한 작가 목록 조회 (페이징 포함)
        List<User> artistList = Optional.ofNullable(queryFactory
                .select(artist)
                .from(follow)
                .join(follow.userArtist, artist)
                .where(follow.user.id.eq(userId))
                .orderBy(follow.createdAt.desc()) // 최근 팔로우 순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()).orElse(List.of());

        // 조회된 작가들의 ID만 추출
        List<Long> artistIds = artistList.stream()
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .toList();

        // 2. 작가들의 아이템과 유저가 스크랩한 여부/시간을 LEFT JOIN으로 함께 조회
        List<Tuple> itemTuples = Optional.ofNullable(queryFactory
                .select(item, scrap.id, scrap.createdAt)
                .from(item)
                .leftJoin(scrap).on(
                        scrap.item.eq(item),
                        scrap.user.id.eq(userId)
                )
                .where(item.user.id.in(artistIds))
                .orderBy(item.createdAt.desc()) // 최신 아이템 우선
                .fetch()).orElse(List.of());

        // 3. 조회된 Tuple을 ItemSearchResponse DTO로 변환하고 작가별로 최대 3개까지만 그룹핑
        Map<Long, List<ItemSearchResponse>> artistItemMap = itemTuples.stream()
                .filter(Objects::nonNull)
                .map(tuple -> {
                    Item i = tuple.get(item);
                    if (i == null) return null;

                    boolean isScrapped = Optional.ofNullable(tuple.get(scrap.id)).isPresent();
                    LocalDateTime scrapedAt = tuple.get(scrap.createdAt);

                    return ItemSearchResponse.fromEntity(i, isScrapped, scrapedAt);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        dto -> dto.getArtist().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.stream().limit(3).toList())
                ));

        // 4. 작가 정보와 아이템 정보를 묶어 FollowedArtistResponse DTO로 변환
        List<FollowedArtistResponse> artistResponses = artistList.stream()
                .filter(Objects::nonNull)
                .map(artistEntity -> {
                    Long artistId = artistEntity.getId();
                    return FollowedArtistResponse.builder()
                            .id(artistId)
                            .profileImgUrl(artistEntity.getImgUrl())
                            .nickname(artistEntity.getNickname())
                            .totalFollower(artistEntity.getFollowerCount()) // DB 필드에서 직접 가져옴
                            .totalItems(artistEntity.getItemCount())       // DB 필드에서 직접 가져옴
                            .isFollowing(true)
                            .items(artistItemMap.getOrDefault(artistId, List.of()))
                            .build();
                }).toList();

        // 5. 해당 유저가 팔로우하고 있는 전체 작가 수 조회 (메타 정보용)
        long totalCount = Optional.ofNullable(queryFactory
                .select(follow.count())
                .from(follow)
                .where(follow.user.id.eq(userId))
                .fetchOne()).orElse(0L);

        // 6. 페이지네이션 메타 정보 생성
        MetaResponse meta = MetaResponse.builder()
                .currentPage(page)
                .pageSize(size)
                .totalItems(totalCount)
                .hasNextPage((long) (safePage + 1) * size < totalCount)
                .build();

        // 7. 최종 응답 DTO 조합 후 반환
        return FollowedArtistsResponse.builder()
                .totalFollowing((int) totalCount)
                .artists(artistResponses)
                .meta(meta)
                .build();
    }


}
