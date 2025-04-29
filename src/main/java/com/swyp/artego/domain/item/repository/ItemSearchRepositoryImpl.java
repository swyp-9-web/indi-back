package com.swyp.artego.domain.item.repository;

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

        int page = request.getPage() != null ? request.getPage() : 0;
        int limit = request.getLimit() != null ? request.getLimit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        var queryBuilder = queryFactory
                .select(new QItemSearchResponse(
                        item.id,
                        item.imgUrls,
                        item.title,
                        item.price,
                        item.categoryType,
                        item.sizeType,
                        item.user.id,
                        item.user.name,
                        (authUser != null) ? scrap.id.isNotNull() : Expressions.constant(false),
                        (authUser != null) ? scrap.createdAt : Expressions.nullExpression(),
                        item.scrapCount,
                        item.likeCount,
                        item.wantCount,
                        item.revisitCount,
                        item.createdAt,
                        item.updatedAt
                ))
                .from(item)
                .innerJoin(item.user, user);

        if (authUser != null) {
            User loginUser = userRepository.findByOauthId(authUser.getOauthId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid auth user"));
            queryBuilder.leftJoin(scrap).on(scrap.item.eq(item).and(scrap.user.id.eq(loginUser.getId())));
        }

        queryBuilder.where(
                        eqKeyword(request.getKeyword()),
                        eqCategoryTypes(request.getCategoryTypes()),
                        eqSizeTypes(request.getSizeTypes()),
                        eqArtistId(request.getArtistId()),
                        filterOnlyScrappedItemsWhenScrapedRecent(request.getSortType())
                )
                .orderBy(orderBySortType(request.getSortType(), authUser != null))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<ItemSearchResponse> items = queryBuilder.fetch();

        Long totalCount = queryFactory
                .select(item.count())
                .from(item)
                .where(
                        eqKeyword(request.getKeyword()),
                        eqCategoryTypes(request.getCategoryTypes()),
                        eqSizeTypes(request.getSizeTypes()),
                        eqArtistId(request.getArtistId()),
                        filterOnlyScrappedItemsWhenScrapedRecent(request.getSortType())
                )
                .fetchOne();

        totalCount = totalCount == null ? 0L : totalCount;

        MetaResponse meta = MetaResponse.builder()
                .currentPage(page)
                .pageSize(limit)
                .totalItems(totalCount)
                .hasNextPage((page + 1) * limit < totalCount)
                .build();

        ConditionsResponse conditions = ConditionsResponse.builder()
                .search(request.getKeyword())
                .filters(new FiltersResponse(
                        request.getSizeTypes(),
                        request.getCategoryTypes()
                ))
                .isLogin(authUser != null)
                .isScrapedPage(request.getSortType() == SortType.SCRAPED_RECENT)
                .isArtistPage(request.getArtistId() != null)
                .sortType(request.getSortType() != null ? request.getSortType() : SortType.CREATED_RECENT)
                .build();

        return ItemSearchResultResponse.builder()
                .items(items)
                .meta(meta)
                .conditions(conditions)
                .build();
    }

    private BooleanExpression eqKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return QItem.item.title.containsIgnoreCase(keyword)
                .or(QItem.item.user.name.containsIgnoreCase(keyword));
    }

    private BooleanExpression eqCategoryTypes(List<CategoryType> categoryTypes) {
        return (categoryTypes != null && !categoryTypes.isEmpty()) ? QItem.item.categoryType.in(categoryTypes) : null;
    }

    private BooleanExpression eqSizeTypes(List<SizeType> sizeTypes) {
        return (sizeTypes != null && !sizeTypes.isEmpty()) ? QItem.item.sizeType.in(sizeTypes) : null;
    }

    private BooleanExpression eqArtistId(Long artistId) {
        return artistId != null ? QItem.item.user.id.eq(artistId) : null;
    }

    private BooleanExpression filterOnlyScrappedItemsWhenScrapedRecent(SortType sortType) {
        if (sortType == SortType.SCRAPED_RECENT) {
            return QScrap.scrap.id.isNotNull();
        }
        return null;
    }

    private OrderSpecifier<?> orderBySortType(SortType sortType, boolean withLogin) {
        QItem item = QItem.item;
        QScrap scrap = QScrap.scrap;

        if (sortType == null) {
            return item.createdAt.desc();
        }

        return switch (sortType) {
            case CREATED_OLDEST -> item.createdAt.asc();
            case SCRAPED_RECENT -> {
                if (!withLogin) throw new IllegalStateException("스크랩 최신순은 로그인 유저만 가능합니다.");
                yield scrap.createdAt.desc();
            }
            case REACTED_TOP -> item.totalReactionCount.desc();
            case SCRAPED_TOP -> item.scrapCount.desc();
            default -> item.createdAt.desc();
        };
    }
}
