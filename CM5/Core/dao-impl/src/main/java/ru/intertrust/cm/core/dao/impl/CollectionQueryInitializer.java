package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.base.CollectionFilterConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.model.FatalException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.applyOffsetAndLimit;

/**
 * Инициализирует запрос для извлечения коллекций, заполняет параметры в конфигурации фильтров, устанавливает порядок сортировки
 * @author atsvetkov
 *
 */
public class CollectionQueryInitializer {

    private static final String PLACEHOLDER_PREFIX = "::";

    private static final String EMPTY_STRING = " ";

    private static final String SQL_DESCENDING_ORDER = "desc";

    private static final String SQL_ASCENDING_ORDER = "asc";

    public static final String QUERY_FILTER_PARAM_DELIMETER = ":";

    public static final String DEFAULT_CRITERIA_CONDITION = "and";

    private ConfigurationExplorer configurationExplorer;

    public CollectionQueryInitializer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Применение фильтров, сортировки и т.д. к прототипу запроса.
     * @param filledFilterConfigs заполненные фильтры
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества
     * @return
     */
    public String initializeQuery(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs,
            SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        String prototypeQuery = collectionConfig.getPrototype();

        String filledQuery =  fillPrototypeQuery(collectionConfig, filledFilterConfigs, sortOrder, offset, limit,
                accessToken,
                prototypeQuery);
        
        filledQuery = processPersonParameter(filledQuery);

        filledQuery = postProcessQuery(collectionConfig, accessToken, filledQuery);

        return filledQuery;

    }

    private String processPersonParameter(String filledQuery) {
        if (filledQuery.indexOf(CollectionsDaoImpl.CURRENT_PERSON_PARAM) > 0) {
            String parameterPrefix = CollectionsDaoImpl.PARAM_NAME_PREFIX;
            filledQuery = CollectionsDaoImpl.adjustParameterNames(filledQuery, parameterPrefix);
        }
        return filledQuery;
    }

    public String initializeQuery(String query, int offset, int limit, AccessToken accessToken) {
        StringBuilder collectionQuery = new StringBuilder(query);
        applyOffsetAndLimit(collectionQuery, offset, limit);
        return postProcessQuery(accessToken, query);
    }

    /**
     * Пост обработка запроса после применения фильтров и правил сортировки. Добавляет поле тип идентификатора доменного объекта и ACL фильтр.
     * @param collectionConfig конфигурация коллекции
     * @param accessToken маркер доступа. В случае отложенного маркера добавляет ACL фильтр.
     * @param query первоначальный запрос
     * @return измененный запрос
     */
    private String postProcessQuery(CollectionConfig collectionConfig, AccessToken accessToken, String query) {
        SqlQueryModifier sqlQueryModifier = new SqlQueryModifier();
        query = sqlQueryModifier.addServiceColumns(query, configurationExplorer);

/*        if (accessToken.isDeferred()) {
            query = sqlQueryModifier.addAclQuery(query, collectionConfig.getIdField());
        }
*/
        sqlQueryModifier.checkDuplicatedColumns(query);

        return query;
    }

    /**
     * Пост обработка запроса после применения фильтров и правил сортировки. Добавляет поле тип идентификатора доменного объекта и ACL фильтр.
     * @param accessToken маркер доступа. В случае отложенного маркера добавляет ACL фильтр.
     * @param query первоначальный запрос
     * @return измененный запрос
     */
    private String postProcessQuery(AccessToken accessToken, String query) {
        SqlQueryModifier sqlQueryModifier = new SqlQueryModifier();
        query = sqlQueryModifier.addServiceColumns(query, configurationExplorer);

/*        if (accessToken.isDeferred()) {
            query = sqlQueryModifier.addAclQuery(query, "id");
        }
*/
        sqlQueryModifier.checkDuplicatedColumns(query);

        return query;
    }

    private String fillPrototypeQuery(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken, String prototypeQuery) {
        if (prototypeQuery == null || prototypeQuery.trim().length() == 0) {
            throw new FatalException("Prototype query is null and can not be processed");
        }
        StringBuilder collectionQuery = new StringBuilder();
        collectionQuery.append(mergeFilledFilterConfigsInPrototypeQuery(prototypeQuery, filledFilterConfigs));


        applySortOrder(sortOrder, collectionQuery);
        applyOffsetAndLimit(collectionQuery, offset, limit);
        return collectionQuery.toString();
    }

    /**
     * Применение фильтров, и т.д. к прототипу запроса на количество доменных объектов в коллекции.
     * @param filledFilterConfigs заполненные фильтры
     * @return
     */
    public String initializeCountQuery(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs, AccessToken accessToken) {
        String prototypeQuery = collectionConfig.getCountingPrototype();
        String filledQuery = fillPrototypeQuery(collectionConfig, filledFilterConfigs, null, 0, 0, accessToken,
                prototypeQuery);

        filledQuery = processPersonParameter(filledQuery);

        filledQuery = postProcessQuery(collectionConfig, accessToken, filledQuery);

        return filledQuery;

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
                String value = collectionFilterConfig.getFilterCriteria().getValue();
                criteriaPlaceHolderCollector.addPlaceholderValue(placeholder, value);
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

        prototypeQuery = removeUnFilledPlaceholders(prototypeQuery);

        return prototypeQuery;
    }

    /**
     * Удаляет не заполненные placeholders в прототипе запроса.
     * @param prototypeQuery исходный запрос
     * @return измененный запрос
     */
    private String removeUnFilledPlaceholders(String prototypeQuery) {
        while(prototypeQuery.indexOf(PLACEHOLDER_PREFIX) > 0){
            int startPlaceHolderIndex = prototypeQuery.indexOf(PLACEHOLDER_PREFIX);
            int endPlaceHolderIndex = prototypeQuery.indexOf(EMPTY_STRING, startPlaceHolderIndex);
            if (endPlaceHolderIndex < 0) {
                endPlaceHolderIndex = prototypeQuery.length();
            }
            String placeHolder = prototypeQuery.substring(startPlaceHolderIndex, endPlaceHolderIndex);

            prototypeQuery = prototypeQuery.replaceAll(placeHolder, "");

        }
        return prototypeQuery;
    }

    private String applySortOrder(SortOrder sortOrder, StringBuilder prototypeQuery) {
        boolean hasSortEntry = false;
        if (sortOrder != null && sortOrder.size() > 0) {
            for (SortCriterion criterion : sortOrder) {
                if (hasSortEntry) {
                    prototypeQuery.append(", ");
                }
                prototypeQuery.append(" order by ").append(criterion.getField()).append("  ").append(getSqlSortOrder(criterion.getOrder()));
                hasSortEntry = true;
            }
        }
        return prototypeQuery.toString();
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
     * из заполненных фильтров. По умолчанию все фильтры соединяются через условие AND ({@link CollectionQueryInitializer#DEFAULT_CRITERIA_CONDITION})
     * @author atsvetkov
     */
    private class CriteriaPlaceHolderCollector {

        private Map<String, String> placeholdersMap = new HashMap<>();

        public void addPlaceholderValue(String placeholder, String value) {
            String placeholderValue = placeholdersMap.get(placeholder);

            if (placeholderValue != null) {
                placeholderValue += createCriteriaValue(value);
            } else {
                placeholderValue = createCriteriaValue(value);
            }
            placeholdersMap.put(placeholder, placeholderValue);

        }

        private String createCriteriaValue(String value) {
            String condition = DEFAULT_CRITERIA_CONDITION;
            StringBuilder criteriaValue = new StringBuilder();
            criteriaValue.append(EMPTY_STRING).append(condition).append(EMPTY_STRING).append(value);
            return criteriaValue.toString();
        }

        public String getPlaceholderValue(String placeholder) {
            return placeholdersMap.get(placeholder);
        }

        public Set<String> getPlaceholders() {
            return placeholdersMap.keySet();
        }
    }

}
