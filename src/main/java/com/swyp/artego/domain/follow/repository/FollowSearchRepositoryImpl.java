package com.swyp.artego.domain.follow.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.artego.domain.follow.dto.response.FollowedArtistResponse;
import com.swyp.artego.domain.follow.dto.response.FollowedArtistsResponse;
import com.swyp.artego.domain.follow.entity.QFollow;
import com.swyp.artego.domain.item.dto.response.ItemSearchResponse;
import com.swyp.artego.domain.item.dto.response.MetaResponse;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.entity.QItem;
import com.swyp.artego.domain.scrap.entity.QScrap;
import com.swyp.artego.domain.user.entity.QUser;
import com.swyp.artego.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FollowSearchRepositoryImpl implements FollowSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public FollowedArtistsResponse findFollowedArtistsWithItems(Long userId, Integer page, Integer size) {
        if (userId == null || page == null || size == null) {
            throw new IllegalArgumentException("userId, page, size는 null일 수 없습니다.");
        }

        QFollow follow = QFollow.follow;
        QUser artist = new QUser("artist");
        QItem item = QItem.item;
        QScrap scrap = QScrap.scrap;

        int safePage = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(safePage, size);

        // 1. 팔로우한 작가 조회
        List<User> artistList = Optional.ofNullable(queryFactory
                .select(artist)
                .from(follow)
                .join(follow.userArtist, artist)
                .where(follow.user.id.eq(userId))
                .orderBy(follow.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()).orElse(List.of());

        List<Long> artistIds = artistList.stream()
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .toList();

        // 2. 작가별 아이템 + 스크랩 LEFT JOIN 조회
        List<Tuple> itemTuples = Optional.ofNullable(queryFactory
                .select(item, scrap.id, scrap.createdAt)
                .from(item)
                .leftJoin(scrap).on(
                        scrap.item.eq(item),
                        scrap.user.id.eq(userId)
                )
                .where(item.user.id.in(artistIds))
                .orderBy(item.createdAt.desc())
                .fetch()).orElse(List.of());

        // 3. 아이템 → DTO 변환 (스크랩 정보 포함)
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

        // 4. 팔로워 수 / 아이템 수 계산
        Map<Long, Long> totalFollowers = getFollowerCountMap(artistIds);
        Map<Long, Long> totalItems = getItemCountMap(artistIds);

        // 5. DTO 변환
        List<FollowedArtistResponse> artistResponses = artistList.stream()
                .filter(Objects::nonNull)
                .map(artistEntity -> {
                    Long artistId = artistEntity.getId();
                    return FollowedArtistResponse.builder()
                            .id(artistId)
                            .profileImgUrl(artistEntity.getImgUrl())
                            .nickname(artistEntity.getName())
                            .totalFollower(totalFollowers.getOrDefault(artistId, 0L).intValue())
                            .totalItems(totalItems.getOrDefault(artistId, 0L).intValue())
                            .isFollowing(true)
                            .items(artistItemMap.getOrDefault(artistId, List.of()))
                            .build();
                }).toList();

        // 6. 전체 팔로우 수
        long totalCount = Optional.ofNullable(queryFactory
                .select(follow.count())
                .from(follow)
                .where(follow.user.id.eq(userId))
                .fetchOne()).orElse(0L);

        // 7. 메타 정보 생성
        MetaResponse meta = MetaResponse.builder()
                .currentPage(page)
                .pageSize(size)
                .totalItems(totalCount)
                .hasNextPage((long) (safePage + 1) * size < totalCount)
                .build();

        return FollowedArtistsResponse.builder()
                .totalFollowing((int) totalCount)
                .artists(artistResponses)
                .meta(meta)
                .build();
    }

    // 작가별 팔로워 수
    private Map<Long, Long> getFollowerCountMap(List<Long> artistIds) {
        QFollow follow = QFollow.follow;

        return Optional.ofNullable(queryFactory
                        .select(follow.userArtist.id, follow.count())
                        .from(follow)
                        .where(follow.userArtist.id.in(artistIds))
                        .groupBy(follow.userArtist.id)
                        .fetch()).orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        tuple -> Objects.requireNonNull(tuple.get(1, Long.class))
                ));
    }

    // 작가별 아이템 수
    private Map<Long, Long> getItemCountMap(List<Long> artistIds) {
        QItem item = QItem.item;

        return Optional.ofNullable(queryFactory
                        .select(item.user.id, item.count())
                        .from(item)
                        .where(item.user.id.in(artistIds))
                        .groupBy(item.user.id)
                        .fetch()).orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        tuple -> Objects.requireNonNull(tuple.get(1, Long.class))
                ));
    }
}
