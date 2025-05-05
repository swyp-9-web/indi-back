package com.swyp.artego.domain.item.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.response.*;
import com.swyp.artego.domain.item.entity.QItem;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.SortType;
import com.swyp.artego.domain.scrap.entity.QScrap;
import com.swyp.artego.domain.user.entity.QUser;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemSearchRepositoryImpl implements ItemSearchRepository {

    private final JPAQueryFactory queryFactory;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request) {
        QItem item = QItem.item;
        QUser user = QUser.user;
        QScrap scrap = QScrap.scrap;

        // 페이지 계산 (1-based → 0-based)
        int pageInput = request.getPage() != null ? request.getPage() : 1;
        int page = Math.max(pageInput - 1, 0);
        int limit = request.getLimit() != null ? request.getLimit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        // 로그인 사용자 조회 및 로그인 여부 boolean 처리
        User loginUser = getLoginUser(authUser);
        boolean isLogin = loginUser != null;

        // 로그인 여부에 따라 프로젝션 필드 정의
        Expression<Boolean> isScrappedExpr = isLogin ? QScrap.scrap.id.isNotNull() : Expressions.constant(false);
        Expression<LocalDateTime> scrapCreatedAtExpr = isLogin ? QScrap.scrap.createdAt : Expressions.nullExpression(LocalDateTime.class);

        // 아이템 검색 쿼리
        var queryBuilder = queryFactory
                .select(new QItemSearchResponse(
                        item.id,
                        item.imgUrls,
                        item.title,
                        item.price,
                        item.categoryType,
                        item.sizeType,
                        item.user.id,
                        item.user.nickname,
                        isScrappedExpr,        // 로그인한 유저가 해당 아이템을 스크랩했는지 여부
                        scrapCreatedAtExpr,    // 스크랩한 시간
                        item.scrapCount,
                        item.likeCount,
                        item.wantCount,
                        item.revisitCount,
                        item.createdAt,
                        item.updatedAt
                ))
                .from(item) // 기준 테이블: 아이템
                .innerJoin(item.user, user) // 필수 관계 조인: 아이템을 올린 유저
                .leftJoin(scrap).on(         // 선택 관계 조인: 로그인 유저가 스크랩한 경우만
                        scrap.item.eq(item),
                        isLogin ? scrap.user.id.eq(loginUser.getId()) : Expressions.FALSE
                )
                .where( // 검색 조건 필터
                        eqStatusTypeOpen(), // 공개 상태인 아이템만
                        eqKeyword(request.getKeyword()), // 키워드 필터 (제목 or 작가명)
                        eqCategoryTypes(request.getCategoryTypes()), // 카테고리 필터
                        eqSizeTypes(request.getSizeTypes()), // 사이즈 필터
                        eqArtistId(request.getArtistId()), // 특정 작가의 아이템만
                        filterOnlyScrappedItemsWhenScrapedRecent(request.getSortType(), isLogin) // 스크랩 최신순일 경우 필터링
                )
                .orderBy(orderBySortType(request.getSortType(), isLogin)) // 정렬 조건
                .offset(pageable.getOffset()) // 페이징 시작 위치
                .limit(pageable.getPageSize()); // 페이지당 개수 제한

        // 결과 fetch
        List<ItemSearchResponse> items = queryBuilder.fetch();

        // 전체 결과 수 조회 (같은 조건, select count)
        Long totalCount = queryFactory
                .select(item.count())
                .from(item)
                .leftJoin(scrap).on(
                        scrap.item.eq(item),
                        isLogin ? scrap.user.id.eq(loginUser.getId()) : Expressions.FALSE
                )
                .where(
                        eqStatusTypeOpen(),
                        eqKeyword(request.getKeyword()),
                        eqCategoryTypes(request.getCategoryTypes()),
                        eqSizeTypes(request.getSizeTypes()),
                        eqArtistId(request.getArtistId()),
                        filterOnlyScrappedItemsWhenScrapedRecent(request.getSortType(), isLogin)
                )
                .fetchOne();

        totalCount = totalCount == null ? 0L : totalCount;

        // 페이지 응답 메타데이터
        MetaResponse meta = MetaResponse.builder()
                .currentPage(page + 1) // 프론트 기준으로 1-based
                .pageSize(limit) // 페이지당 개수
                .totalItems(totalCount) // 전체 아이템 수
                .hasNextPage((page + 1) * limit < totalCount) // 다음 페이지 존재 여부
                .build();

        // 클라이언트에게 전달할 검색 조건 상태 정보
        ConditionsResponse conditions = ConditionsResponse.builder()
                .search(request.getKeyword()) // 검색어
                .filters(new FiltersResponse(
                        request.getSizeTypes(), // 선택한 사이즈 필터
                        request.getCategoryTypes() // 선택한 카테고리 필터
                ))
                .isLogin(isLogin) // 로그인 여부
                .isScrapedPage(request.getSortType() == SortType.SCRAPED_RECENT) // 스크랩 탭인지 여부
                .isArtistPage(request.getArtistId() != null) // 작가 페이지 여부
                .sortType(request.getSortType() != null ? request.getSortType() : SortType.CREATED_RECENT) // 정렬 기준
                .build();

        // 최종 검색 결과 반환
        return ItemSearchResultResponse.builder()
                .items(items)
                .meta(meta)
                .conditions(conditions)
                .build();
    }

    // OAuth ID를 기반으로 로그인 사용자 조회
    private User getLoginUser(AuthUser authUser) {
        if (authUser == null) return null;
        return userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid auth user"));
    }

    // 키워드가 제목 또는 작가명에 포함되어 있는지 검사
    private BooleanExpression eqKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return QItem.item.title.containsIgnoreCase(keyword)
                .or(QItem.item.user.name.containsIgnoreCase(keyword));
    }

    // 카테고리 필터 적용
    private BooleanExpression eqCategoryTypes(List<CategoryType> categoryTypes) {
        return (categoryTypes != null && !categoryTypes.isEmpty())
                ? QItem.item.categoryType.in(categoryTypes)
                : null;
    }

    // 사이즈 필터 적용
    private BooleanExpression eqSizeTypes(List<SizeType> sizeTypes) {
        return (sizeTypes != null && !sizeTypes.isEmpty())
                ? QItem.item.sizeType.in(sizeTypes)
                : null;
    }

    // 특정 작가의 아이템만 조회 (작가 페이지에서 사용)
    private BooleanExpression eqArtistId(Long artistId) {
        return artistId != null ? QItem.item.user.id.eq(artistId) : null;
    }

    // 상태가 OPEN인 아이템만 조회 (공개 상태)
    private BooleanExpression eqStatusTypeOpen() {
        return QItem.item.statusType.eq(com.swyp.artego.domain.item.enums.StatusType.OPEN);
    }

    // 스크랩된 게시글만 필터링 (스크랩 탭 or 최신순일 때)
    private BooleanExpression filterOnlyScrappedItemsWhenScrapedRecent(SortType sortType, boolean isLogin) {
        if ((sortType == SortType.SCRAPED_RECENT || sortType == SortType.SCRAP_ITEM_RECENT) && isLogin) {
            return QScrap.scrap.id.isNotNull();
        }
        return null;
    }

    // 정렬 기준에 따른 OrderSpecifier 설정
    private OrderSpecifier<?> orderBySortType(SortType sortType, boolean isLogin) {
        QItem item = QItem.item;
        QScrap scrap = QScrap.scrap;

        if (sortType == null) return item.createdAt.desc();

        return switch (sortType) {
            case CREATED_OLDEST -> item.createdAt.asc();
            case SCRAPED_RECENT -> {
                if (!isLogin) throw new IllegalStateException("스크랩 최신순은 로그인 유저만 가능합니다.");
                yield scrap.createdAt.desc();
            }
            case SCRAP_ITEM_RECENT -> {
                if (!isLogin) throw new IllegalStateException("스크랩한 게시글 최신순은 로그인 유저만 가능합니다.");
                yield item.createdAt.desc();
            }
            case REACTED_TOP -> item.totalReactionCount.desc();
            case SCRAPED_TOP -> item.scrapCount.desc();
            default -> item.createdAt.desc();
        };
    }
}
