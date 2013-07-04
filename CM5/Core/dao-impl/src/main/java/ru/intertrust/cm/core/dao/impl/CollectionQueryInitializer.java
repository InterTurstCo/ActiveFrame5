package ru.intertrust.cm.core.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.dao.exception.CollectionConfigurationException;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Инициализирует запрос для извлечения коллекций, заполняет параметры в конфигурации фильтров, устанавливает порядок сортировки
 * @author atsvetkov
 *
 */
public class CollectionQueryInitializer {

    private static final String PLACEHOLDER_PREFIX = "::";

    private static final String DEFAULT_CRITERIA = " 1=1 ";

    private static final String EMPTY_STRING = " ";

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
        if(prototypeQuery == null || prototypeQuery.trim().length() == 0){
            throw new FatalException("Prototype query is null and can not be processed");
        }
        String collectionQuery = mergeFilledFilterConfigsInPrototypeQuery(prototypeQuery, filledFilterConfigs);
        collectionQuery = applySortOrder(sortOrder, collectionQuery);
        collectionQuery = applyLimitAndOffset(offset, limit, collectionQuery);
        return collectionQuery;

    }

    /**
     * Применение фильтров, и т.д. к прототипу запроса на количество доменных объектов в коллекции.
     * @param prototypeQuery прототип запроса
     * @param filledFilterConfigs заполненные фильтры
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
        
        ReferencePlaceHolderCollector referencePlaceHolderCollector = new ReferencePlaceHolderCollector();
        CriteriaPlaceHolderCollector criteriaPlaceHolderCollector = new CriteriaPlaceHolderCollector();
        
        for (CollectionFilterConfig collectionFilterConfig : filledFilterConfigs) {
            if (collectionFilterConfig.getFilterReference() != null
                    && collectionFilterConfig.getFilterReference().getPlaceholder() != null) {
                String placeholder = collectionFilterConfig.getFilterReference().getPlaceholder();
                String value = collectionFilterConfig.getFilterReference().getValue();
                referencePlaceHolderCollector.addPlaceholderValue(placeholder, value);
            }

            if (collectionFilterConfig.getFilterCriteria() != null
                    && collectionFilterConfig.getFilterCriteria().getPlaceholder() != null) {
                String placeholder = collectionFilterConfig.getFilterCriteria().getPlaceholder();
                String condition = collectionFilterConfig.getFilterCriteria().getCondition();
                String value = collectionFilterConfig.getFilterCriteria().getValue();
                criteriaPlaceHolderCollector.addPlaceholderValue(placeholder, condition, value);
            }
        }
        
        for (String placeholder : referencePlaceHolderCollector.getPlaceholders()) {
            String placeholderValue = referencePlaceHolderCollector.getPlaceholderValue(placeholder);
            prototypeQuery = prototypeQuery.replace(PLACEHOLDER_PREFIX + placeholder, placeholderValue);
        }

        for (String placeholder : criteriaPlaceHolderCollector.getPlaceholders()) {
            String placeholderValue = criteriaPlaceHolderCollector.getPlaceholderValue(placeholder);
            if (placeholderValue == null) {
                placeholderValue = EMPTY_STRING;
            }

            prototypeQuery = prototypeQuery.replace(PLACEHOLDER_PREFIX + placeholder, placeholderValue);
        }

        if (prototypeQuery.indexOf(PLACEHOLDER_PREFIX) > 0) {
            throw new CollectionConfigurationException("Prototype query was not filled correctly: " + prototypeQuery
                    + " Please verify all required parameters are passed.");
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
    
    /**
     * Группирует фильтры после кл. слова from по названию placeholder.
     * @author atsvetkov
     */
    private class ReferencePlaceHolderCollector {

        private Map<String, String> placeholdersMap = new HashMap<>();

        public void addPlaceholderValue(String placeholder, String value) {
            String placeholderValue = placeholdersMap.get(placeholder);

            if (placeholderValue != null) {
                placeholderValue += value;
            } else {
                placeholderValue = value;
            }
            placeholdersMap.put(placeholder, placeholderValue);

        }

        public String getPlaceholderValue(String placeholder) {
            return placeholdersMap.get(placeholder);
        }

        public Set<String> getPlaceholders() {
            return placeholdersMap.keySet();
        }
    }
 
    /**
     * Группирует все фильтры после слова where по названию placeholder. Т.е. для каждого placeholder составляет запрос
     * из заполненных фильтров.
     * @author atsvetkov
     */
    private class CriteriaPlaceHolderCollector {

        private Map<String, String> placeholdersMap = new HashMap<>();

        public void addPlaceholderValue(String placeholder, String condition, String value) {
            String placeholderValue = placeholdersMap.get(placeholder);

            if (condition == null) {
                condition = DEFAULT_CRITERIA_CONDITION;
            }
            if (placeholderValue != null) {
                placeholderValue += EMPTY_STRING + condition + EMPTY_STRING + value;
            } else {
                placeholderValue = EMPTY_STRING + value;
            }
            placeholdersMap.put(placeholder, placeholderValue);

        }

        public String getPlaceholderValue(String placeholder) {
            return placeholdersMap.get(placeholder);
        }

        public Set<String> getPlaceholders() {
            return placeholdersMap.keySet();
        }
    }
    
}
