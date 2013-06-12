package ru.intertrust.cm.core.dao.impl;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.config.CollectionFilterConfig;

/**
 * Инициализирует запрос для извлечения коллекций, заполняет параметры в конфигурации фильтров, устанавливает порядок сортировки
 * @author atsvetkov
 * 
 */
public class CollectionQueryInitializer {

    private static final String DEFAULT_CRITERIA = " 1=1 ";
    
    private static final String EMPTY_PLACEHOLDER = " ";

    private static final String CRITERIA_PLACEHOLDER = "::where-clause";

    private static final String REFERENCE_PLACEHOLDER = "::from-clause";

    private static final String SQL_DESCENDING_ORDER = "desc";

    private static final String SQL_ASCENDING_ORDER = "asc";

    public static final String QUERY_FILTER_PARAM_DELIMETER = ":";
    
    public static final String DEFAULT_CRITERIA_CONDITION = "and";

    /**
     * Применение фильтров, сортировки и т.д. к прототипу запроса.
     * @param prototypeQuery прототип запроса
     * @param filledFilterConfigs заполненные фильтры
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества
     * @return
     */
    public String initializeQuery(String prototypeQuery, List<CollectionFilterConfig> filledFilterConfigs, SortOrder sortOrder, int offset, int limit) {
        String collectionQuery = mergeFilledFilterConfigsInPrototypeQuery(prototypeQuery, filledFilterConfigs);        
        collectionQuery = applySortOrder(sortOrder, collectionQuery);
        collectionQuery = applyLimitAndOffset(offset, limit, collectionQuery);
        return collectionQuery;
        
    }

    /**
     * Применение фильтров, и т.д. к прототипу запроса на количество бизнес-объектов в коллекции.
     * @param prototypeQuery прототип запроса
     * @param filledFilterConfigs заполненные фильтры
     * @param sortOrder порядок сортировки
     * @return
     */
    public String initializeCountQuery(String prototypeQuery, List<CollectionFilterConfig> filledFilterConfigs) {
        return initializeQuery(prototypeQuery, filledFilterConfigs, null, 0, 0);
    }
    
    private String applyLimitAndOffset(int offset, int limit, String collectionQuery) {
        if (limit != 0) {
            collectionQuery += " limit " + limit + " OFFSET " + offset;
        }
        return collectionQuery;
    }

    private String mergeFilledFilterConfigsInPrototypeQuery(String prototypeQuery, List<CollectionFilterConfig> filledFilterConfigs) {
        StringBuilder mergedFilterCriteria = new StringBuilder();
        StringBuilder mergedFilterReference = new StringBuilder();

        boolean hasEntry = false;
        for (CollectionFilterConfig collectionFilterConfig : filledFilterConfigs) {

            if (collectionFilterConfig.getFilterReference() != null) {
                mergedFilterReference.append(collectionFilterConfig.getFilterReference().getValue());
            }
            if (hasEntry) {
                mergedFilterCriteria.append(EMPTY_PLACEHOLDER);
                if (collectionFilterConfig.getFilterCriteria().getCondition() != null) {
                    mergedFilterCriteria.append(collectionFilterConfig.getFilterCriteria().getCondition());

                } else {
                    mergedFilterCriteria.append(DEFAULT_CRITERIA_CONDITION);

                }
                mergedFilterCriteria.append(EMPTY_PLACEHOLDER);
            }
            mergedFilterCriteria.append(collectionFilterConfig.getFilterCriteria().getValue());
            hasEntry = true;
        }

        prototypeQuery = applyMergedFilterReference(prototypeQuery, mergedFilterReference.toString());

        prototypeQuery = applyMergedFilterCriteria(prototypeQuery, mergedFilterCriteria.toString());
        return prototypeQuery;
    }

    private String applyMergedFilterCriteria(String prototypeQuery, String mergedFilterCriteria) {
        if (mergedFilterCriteria.length() > 0) {
            prototypeQuery = prototypeQuery.replaceAll(CRITERIA_PLACEHOLDER, mergedFilterCriteria);
        } else {
            prototypeQuery = prototypeQuery.replaceAll(CRITERIA_PLACEHOLDER, DEFAULT_CRITERIA);
        }
        return prototypeQuery;
    }

    private String applyMergedFilterReference(String prototypeQuery, String mergedFilterReference) {
        if (mergedFilterReference.length() > 0) {
            prototypeQuery = prototypeQuery.replaceAll(REFERENCE_PLACEHOLDER, mergedFilterReference);
        } else {
            prototypeQuery = prototypeQuery.replaceAll(REFERENCE_PLACEHOLDER, EMPTY_PLACEHOLDER);
        }
        return prototypeQuery;
    }

    private String applySortOrder(SortOrder sortOrder, String prototypeQuery) {
        StringBuilder prototypeQueryBuilder = new StringBuilder(prototypeQuery);

        boolean hasSortEntry = false;
        if (sortOrder != null && sortOrder.size() > 0) {
            for (SortCriterion criterion : sortOrder) {
                if (hasSortEntry) {
                    prototypeQueryBuilder.append(", ");
                }
                prototypeQueryBuilder.append(" order by ").append(criterion.getField()).append("  ").append(getSqlSortOrder(criterion.getOrder()));
                hasSortEntry = true;
            }
        }
        return prototypeQueryBuilder.toString();
    }

    private String getSqlSortOrder(SortCriterion.Order order) {
        if (order == Order.ASCENDING) {
            return SQL_ASCENDING_ORDER;
        } else if (order == Order.DESCENDING) {
            return SQL_DESCENDING_ORDER;
        } else {
            return SQL_ASCENDING_ORDER;
        }
    }
}
