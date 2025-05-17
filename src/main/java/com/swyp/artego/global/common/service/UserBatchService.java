package com.swyp.artego.global.common.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.artego.domain.follow.entity.QFollow;
import com.swyp.artego.domain.item.entity.QItem;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.itemEmoji.entity.QItemEmoji;
import com.swyp.artego.domain.scrap.entity.QScrap;
import com.swyp.artego.domain.user.entity.QUser;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import com.swyp.artego.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserBatchService {

    // TODO : 나중에 유저가 많아지면 나눠서 진행해야 한다.
    private final JPAQueryFactory queryFactory;
    private final UserRepository userRepository;

    private final QItem item = QItem.item;
    private final QScrap scrap = QScrap.scrap;
    private final QItemEmoji emoji = QItemEmoji.itemEmoji;
    private final QFollow follow = QFollow.follow;
    private final QUser user = QUser.user;

    @Transactional
    @Scheduled(initialDelay = 5000,fixedRate = 1000 * 60 * 10) // 10분 간격
    //@Scheduled(initialDelay = 5000, fixedRate = 1000 * 60)
    public void updateUserStatistics() {

        // 1. 작가별 아이템 수
        Map<Long, Long> itemCountMap = queryFactory
                .select(item.user.id, item.count())
                .from(item)
                .where(item.statusType.eq(StatusType.OPEN)
                        .and(item.user.role.eq(Role.ARTIST))
                        .and(item.user.banned.isFalse())
                        .and(item.user.deleted.isFalse()))
                .groupBy(item.user.id)
                .fetch()
                .stream()
                .collect(HashMap::new,
                        (map, tuple) -> map.put(tuple.get(item.user.id), tuple.get(item.count())),
                        HashMap::putAll);

        // 2. 작가별 스크랩 수
        Map<Long, Long> scrapCountMap = queryFactory
                .select(item.user.id, scrap.count())
                .from(scrap)
                .join(scrap.item, item)
                .where(item.statusType.eq(StatusType.OPEN)
                        .and(item.user.role.eq(Role.ARTIST))
                        .and(item.user.banned.isFalse())
                        .and(item.user.deleted.isFalse()))
                .groupBy(item.user.id)
                .fetch()
                .stream()
                .collect(HashMap::new,
                        (map, tuple) -> map.put(tuple.get(item.user.id), tuple.get(scrap.count())),
                        HashMap::putAll);



        // 3. 작가별 리액션 수
        Map<Long, Long> reactionCountMap = queryFactory
                .select(item.user.id, emoji.count())
                .from(emoji)
                .join(emoji.item, item)
                .where(item.statusType.eq(StatusType.OPEN)
                        .and(item.user.role.eq(Role.ARTIST))
                        .and(item.user.banned.isFalse())
                        .and(item.user.deleted.isFalse()))
                .groupBy(item.user.id)
                .fetch()
                .stream()
                .collect(HashMap::new,
                        (map, tuple) -> map.put(tuple.get(item.user.id), tuple.get(emoji.count())),
                        HashMap::putAll);

        // 4. 작가별 팔로워 수
        Map<Long, Long> followerCountMap = queryFactory
                .select(follow.userArtist.id, follow.count())
                .from(follow)
                .join(follow.userArtist, user)
                .where(user.role.eq(Role.ARTIST)
                        .and(user.banned.isFalse())
                        .and(user.deleted.isFalse()))
                .groupBy(follow.userArtist.id)
                .fetch()
                .stream()
                .collect(HashMap::new,
                        (map, tuple) -> map.put(tuple.get(follow.userArtist.id), tuple.get(follow.count())),
                        HashMap::putAll);

        // 5. 활동 중인 작가들 조회
        List<User> artists = queryFactory
                .selectFrom(user)
                .where(user.role.eq(Role.ARTIST)
                        .and(user.banned.isFalse())
                        .and(user.deleted.isFalse()))
                .fetch();

        // 6. 각각 업데이트
        for (User artist : artists) {
            Long userId = artist.getId();
            artist.setItemCount(toInt(itemCountMap.get(userId)));
            artist.setScrapCount(toInt(scrapCountMap.get(userId)));
            artist.setReactionCount(toInt(reactionCountMap.get(userId)));
            artist.setFollowerCount(toInt(followerCountMap.get(userId)));
        }

        // 7. 저장
        userRepository.saveAll(artists);
    }

    private int toInt(Long value) {
        return value != null ? value.intValue() : 0;
    }


}
