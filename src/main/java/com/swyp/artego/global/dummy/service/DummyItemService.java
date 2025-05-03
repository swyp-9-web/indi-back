package com.swyp.artego.global.dummy.service;

import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.itemEmoji.enums.EmojiType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DummyItemService {

    private final JdbcTemplate jdbcTemplate;

    private static final List<String> DUMMY_IMG_URLS = List.of(
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/e96f5ed4-ff1b-4c54-9a40-e4427af53f57.jpg",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/d90998c8-744a-4b28-bf2f-7bb3059d6b89.jpg",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/7f9d1b18-99c2-4bbd-af82-54748d6bccdf.jpg",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/13901bdc-c2f3-4694-abca-023568f7a1df.jpg",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/79bd9140-3e74-4190-a0e6-b87ae3f3c497.jpg",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/2018d16d-4883-4836-a8e5-79146aba84bf.png",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/5700638a-19d4-4f8a-a2ba-d353bbf492e2.jpg",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/34a4cd95-27d0-4d2c-8429-e3174b579d20.jpg",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/f643bf41-ea45-44cf-8ef0-2cb2f6e07a24.png",
            "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/b3db25fe-5e0e-485e-b342-91ee1239950d.jpg"
    );

    private static final List<String> MATERIALS = List.of("캔버스", "종이", "나무", "아크릴");

    private static final List<String> ARTIST_NAMES = List.of(
            "모노그래피", "빈티지블루", "달의조각", "코튼무드", "한낮의별",
            "잉크드림", "살랑살랑", "캔버스위치", "물빛공방", "흑백의술사",
            "마르지않는꽃", "햇살도감", "아트코코", "봄의수채화", "하루드로잉",
            "노을빛작가", "미니멀몽", "밤하늘노트", "페인트폭스", "투명정원"
    );

    private static final List<String> ITEM_TITLES = List.of(
            "푸른 정오", "별의 기억", "나무 아래에서", "비 오는 오후", "작은 숨결",
            "흐르는 선", "오래된 여백", "밤의 질감", "고요한 선율", "감정의 틈",
            "붓 끝의 향기", "빛과 그림자", "시선의 자리", "일상의 조각", "그날의 무늬"
    );

    private static final String DEFAULT_PROFILE_IMG_URL = "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/b3db25fe-5e0e-485e-b342-91ee1239950d.jpg";

    @Transactional
    public void createDummyData(int count) {
        Random random = new Random();
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            userIds.add(insertDummyUser());
        }

        List<SizeType> sizeTypes = generateBalancedSizeTypes(count);
        List<Object[]> itemBatch = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        String insertItemSql = """
            INSERT INTO item (user_id, title, description, img_urls, price, size, size_width, size_heigth, size_depth, material, status, category_type, scrap_count, like_count, want_count, revisit_count, total_reaction_score, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        for (int i = 0; i < count; i++) {
            Long userId = userIds.get(random.nextInt(userIds.size()));

            int width = random.nextInt(30) + 10;
            int height = random.nextInt(30) + 10;
            int depth = random.nextInt(30) + 10;
            SizeType sizeType = sizeTypes.get(i);

            String imageUrlJson = "[\"" + DUMMY_IMG_URLS.get(random.nextInt(DUMMY_IMG_URLS.size())) + "\"]";
            String category = CategoryType.values()[random.nextInt(CategoryType.values().length)].name();
            String status = StatusType.OPEN.name();
            String title = ITEM_TITLES.get(random.nextInt(ITEM_TITLES.size())) + " #" + i;

            int scrapCount = random.nextInt(userIds.size());
            int like = random.nextInt(5);
            int want = random.nextInt(5);
            int revisit = random.nextInt(5);
            int totalReaction = like + want + revisit;

            itemBatch.add(new Object[]{
                    userId,
                    title,
                    "감성적인 작품 설명입니다. #" + i,
                    imageUrlJson,
                    10000 + random.nextInt(90000),
                    sizeType.name(),
                    width,
                    height,
                    depth,
                    MATERIALS.get(random.nextInt(MATERIALS.size())),
                    status,
                    category,
                    scrapCount,
                    like,
                    want,
                    revisit,
                    totalReaction,
                    now,
                    now
            });
        }

        jdbcTemplate.batchUpdate(insertItemSql, itemBatch);

        List<Long> itemIds = jdbcTemplate.query(
                "SELECT item_id FROM item ORDER BY item_id DESC LIMIT ?",
                (rs, rowNum) -> rs.getLong("item_id"),
                count
        );

        List<Object[]> scrapBatch = new ArrayList<>();
        List<Object[]> emojiBatch = new ArrayList<>();

        for (Long itemId : itemIds) {
            Collections.shuffle(userIds);
            int scrapSize = random.nextInt(userIds.size());

            for (int i = 0; i < scrapSize; i++) {
                Long userId = userIds.get(i);
                scrapBatch.add(new Object[]{userId, itemId, now, now});
            }

            Collections.shuffle(userIds);
            int emojiSize = random.nextInt(userIds.size());

            for (int i = 0; i < emojiSize; i++) {
                Long userId = userIds.get(i);
                EmojiType emoji = EmojiType.values()[random.nextInt(EmojiType.values().length)];
                emojiBatch.add(new Object[]{itemId, userId, emoji.name(), now, now});
            }
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO scrap (user_id, item_id, created_at, updated_at) VALUES (?, ?, ?, ?)",
                scrapBatch
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO item_emoji (item_id, user_id, emoji_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                emojiBatch
        );
    }

    private Long insertDummyUser() {
        String sql = "INSERT INTO `user` (oauth_id, name, email, tel_number, img_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String oauthId = "dummy_" + uuid;
        String email = "user" + uuid + "@artego.com";
        String telNumber = "010-" + (1000 + random.nextInt(9000)) + "-" + (1000 + random.nextInt(9000));
        String nickname = ARTIST_NAMES.get(random.nextInt(ARTIST_NAMES.size()));

        jdbcTemplate.update(sql, oauthId, nickname, email, telNumber, DEFAULT_PROFILE_IMG_URL, now, now);

        return jdbcTemplate.queryForObject(
                "SELECT user_id FROM `user` WHERE oauth_id = ?",
                Long.class,
                oauthId
        );
    }

    private List<SizeType> generateBalancedSizeTypes(int total) {
        List<SizeType> result = new ArrayList<>();
        int base = total / 3;
        int remainder = total % 3;

        for (int i = 0; i < base; i++) result.add(SizeType.S);
        for (int i = 0; i < base; i++) result.add(SizeType.M);
        for (int i = 0; i < base; i++) result.add(SizeType.L);

        if (remainder > 0) result.add(SizeType.S);
        if (remainder > 1) result.add(SizeType.M);

        Collections.shuffle(result);
        return result;
    }
}
