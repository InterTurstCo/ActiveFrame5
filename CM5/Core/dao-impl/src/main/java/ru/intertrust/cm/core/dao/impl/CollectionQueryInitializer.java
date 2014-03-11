package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.model.FatalException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Инициализирует запрос для извлечения коллекций, заполняет параметры в конфигурации фильтров, устанавливает порядок сортировки
 * @author atsvetkov
 *
 */
public class CollectionQueryInitializer {

    private static final String PLACEHOLDER_REGEXP_PATTERN = "::[\\w\\d_\\-]+";

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
     * @param filterValues заполненные фильтры
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества
     * @return
     */
    public String initializeQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        String prototypeQuery = collectionConfig.getPrototype();

        String filledQuery =  fillPrototypeQuery(collectionConfig, filledFilterConfigs, sortOrder, offset, limit,
                accessToken,
                prototypeQuery);

        filledQuery = processPersonParameter(filledQuery);

        filledQuery = postProcessQuery(collectionConfig, filterValues, accessToken, filledQuery);

        return filledQuery;

    }

    /**
     * Заполняет конфигурации фильтров значениями. Возвращает заполненные конфигурации фильтров (для которых были
     * переданы значения). Сделан публичным для тестов.
     * @param filterValues
     * @param collectionConfig
     * @return
     */
    private List<CollectionFilterConfig> findFilledFilterConfigs(List<? extends Filter> filterValues,
                                                                 CollectionConfig collectionConfig) {
        List<CollectionFilterConfig> filterConfigs = collectionConfig.getFilters();

        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<CollectionFilterConfig>();

        if (filterConfigs == null || filterValues == null) {
            return filledFilterConfigs;
        }

        for (CollectionFilterConfig filterConfig : filterConfigs) {
            for (Filter filterValue : filterValues) {
                if (!filterConfig.getName().equals(filterValue.getFilter())) {
                    continue;
                }
                CollectionFilterConfig filledFilterConfig = replaceFilterCriteriaParam(filterConfig, filterValue);
                filledFilterConfigs.add(filledFilterConfig);

            }
        }
        return filledFilterConfigs;
    }

    private CollectionFilterConfig replaceFilterCriteriaParam(CollectionFilterConfig filterConfig, Filter filterValue) {
        CollectionFilterConfig clonedFilterConfig = cloneFilterConfig(filterConfig);

        String criteria = clonedFilterConfig.getFilterCriteria().getValue();
        String filterName = filterValue.getFilter();

        String parameterPrefix = CollectionsDaoImpl.PARAM_NAME_PREFIX + filterName;
        String newFilterCriteria = CollectionsDaoImpl.adjustParameterNames(criteria, parameterPrefix);

        clonedFilterConfig.getFilterCriteria().setValue(newFilterCriteria);
        return clonedFilterConfig;
    }

    /**
     * Клонирует конфигурацию коллекции. При заполнении параметров в фильтрах нужно, чтобы первоначальная конфигурация
     * коллекции оставалась неизменной.
     * @param filterConfig конфигурации коллекции
     * @return копия переданной конфигурации коллекции
     */
    private CollectionFilterConfig cloneFilterConfig(CollectionFilterConfig filterConfig) {
        CollectionFilterConfig clonedFilterConfig = new CollectionFilterConfig();

        CollectionFilterReferenceConfig srcFilterReference = filterConfig.getFilterReference();
        if (srcFilterReference != null) {
            CollectionFilterReferenceConfig clonedFilterReference = new CollectionFilterReferenceConfig();
            clonedFilterReference.setPlaceholder(srcFilterReference.getPlaceholder());
            clonedFilterReference.setValue(srcFilterReference.getValue());
            clonedFilterConfig.setFilterReference(clonedFilterReference);
        }

        CollectionFilterCriteriaConfig srcFilterCriteria = filterConfig.getFilterCriteria();
        if (srcFilterCriteria != null) {
            CollectionFilterCriteriaConfig clonedFilterCriteria = new CollectionFilterCriteriaConfig();
            clonedFilterCriteria.setPlaceholder(srcFilterCriteria.getPlaceholder());
            clonedFilterCriteria.setValue(srcFilterCriteria.getValue());
            clonedFilterConfig.setFilterCriteria(clonedFilterCriteria);
        }

        clonedFilterConfig.setName(filterConfig.getName());

        return clonedFilterConfig;
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
        DaoUtils.applyOffsetAndLimit(collectionQuery, offset, limit);
        return postProcessQuery(accessToken, collectionQuery.toString());
    }

    /**
     * Пост обработка запроса после применения фильтров и правил сортировки. Добавляет поле тип идентификатора доменного объекта и ACL фильтр.
     * @param collectionConfig конфигурация коллекции
     * @param accessToken маркер доступа. В случае отложенного маркера добавляет ACL фильтр.
     * @param query первоначальный запрос
     * @return измененный запрос
     */
    private String postProcessQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues,
            AccessToken accessToken, String query) {
        SqlQueryModifier sqlQueryModifier = new SqlQueryModifier(configurationExplorer);
        query = sqlQueryModifier.addServiceColumns(query);
        query = sqlQueryModifier.addIdBasedFilters(query, filterValues, collectionConfig.getIdField());

        if (accessToken.isDeferred()) {
            query = sqlQueryModifier.addAclQuery(query);
        }

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
        SqlQueryModifier sqlQueryModifier = new SqlQueryModifier(configurationExplorer);
        query = sqlQueryModifier.addServiceColumns(query);

        if (accessToken.isDeferred()) {
            query = sqlQueryModifier.addAclQuery(query);
        }

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
        DaoUtils.applyOffsetAndLimit(collectionQuery, offset, limit);
        return collectionQuery.toString();
    }

    /**
     * Применение фильтров, и т.д. к прототипу запроса на количество доменных объектов в коллекции.
     * @param filterValues заполненные фильтры
     * @return
     */
    public String initializeCountQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues,
            AccessToken accessToken) {
        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        String prototypeQuery = collectionConfig.getCountingPrototype();
        String filledQuery = fillPrototypeQuery(collectionConfig, filledFilterConfigs, null, 0, 0, accessToken,
                prototypeQuery);

        filledQuery = processPersonParameter(filledQuery);

        filledQuery = postProcessQuery(collectionConfig, filterValues, accessToken, filledQuery);

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
        Pattern pattern = Pattern.compile(PLACEHOLDER_REGEXP_PATTERN);
        Matcher matcher = pattern.matcher(prototypeQuery);
        // Check all occurrences
        while (matcher.find()) {
            prototypeQuery = matcher.replaceAll("");
        }
        return prototypeQuery;
    }

    private String applySortOrder(SortOrder sortOrder, StringBuilder prototypeQuery) {
        boolean hasSortEntry = false;
        if (sortOrder != null && sortOrder.size() > 0) {
            for (SortCriterion criterion : sortOrder) {
                if (!hasSortEntry) {
                    prototypeQuery.append(" order by ");
                } else {
                    prototypeQuery.append(", ");
                }
                prototypeQuery.append(criterion.getField()).append("  ").append(getSqlSortOrder(criterion.getOrder()));
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
