package com.swyp.artego.global.common.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.entity.QItem;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.itemEmoji.entity.QItemEmoji;
import com.swyp.artego.domain.itemEmoji.enums.EmojiType;
import com.swyp.artego.domain.scrap.entity.QScrap;
import com.swyp.artego.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemBatchService {

    private final JPAQueryFactory queryFactory;
    private final ItemRepository itemRepository;

    private final QScrap scrap = QScrap.scrap;
    private final QItemEmoji emoji = QItemEmoji.itemEmoji;

    @Transactional
    @Scheduled(initialDelay = 10000, fixedRate = 1000 * 60 * 120) // 앱 시작 후 10초 뒤, 2시간마다 실행
    //@Scheduled(initialDelay = 5000, fixedRate = 5000) // 앱 시작 후 10초 뒤, 5초마다 실행 => 테스트용
    public void updateItemStatistics() {


        List<Item> items = queryFactory
                .selectFrom(QItem.item)
                .join(QItem.item.user, QUser.user).fetchJoin()
                .where(QItem.item.statusType.eq(StatusType.OPEN))
                .fetch();

        // 1. 스크랩 수 Map<itemId, count>
        List<Tuple> scrapTuples = queryFactory
                .select(scrap.item.id, scrap.count())
                .from(scrap)
                .groupBy(scrap.item.id)
                .fetch();

        Map<Long, Long> scrapCountMap = new HashMap<>();
        for (Tuple tuple : scrapTuples) {
            Long itemId = tuple.get(scrap.item.id);
            Long count = tuple.get(scrap.count());
            scrapCountMap.put(itemId, count);
        }

        // 2. 이모지 수 Map<itemId, Map<EmojiType, count>>
        List<Tuple> emojiTuples = queryFactory
                .select(emoji.item.id, emoji.emojiType, emoji.count())
                .from(emoji)
                .groupBy(emoji.item.id, emoji.emojiType)
                .fetch();

        Map<Long, Map<EmojiType, Long>> emojiCountMap = new HashMap<>();
        for (Tuple tuple : emojiTuples) {
            Long itemId = tuple.get(emoji.item.id);
            EmojiType emojiType = tuple.get(emoji.emojiType);
            Long count = tuple.get(emoji.count());

            emojiCountMap
                    .computeIfAbsent(itemId, k -> new HashMap<>())
                    .put(emojiType, count);
        }

        // 3. 아이템별 수치 업데이트
        for (Item item : items) {
            Long itemId = item.getId();

            int scrapCount = safeLongToInt(scrapCountMap.get(itemId));
            int likeCount = safeLongToInt(getEmojiCount(emojiCountMap, itemId, EmojiType.LIKES));
            int wantCount = safeLongToInt(getEmojiCount(emojiCountMap, itemId, EmojiType.WANTS));
            int revisitCount = safeLongToInt(getEmojiCount(emojiCountMap, itemId, EmojiType.REVISITS));
            int totalReaction = likeCount + wantCount + revisitCount;

            item.updateCounts(scrapCount, likeCount, wantCount, revisitCount, totalReaction);
        }
    }

    private int safeLongToInt(Long value) {
        return (value != null) ? value.intValue() : 0;
    }

    private Long getEmojiCount(Map<Long, Map<EmojiType, Long>> emojiCountMap, Long itemId, EmojiType type) {
        Map<EmojiType, Long> typeCountMap = emojiCountMap.getOrDefault(itemId, Map.of());
        return typeCountMap.getOrDefault(type, 0L);
    }
}
