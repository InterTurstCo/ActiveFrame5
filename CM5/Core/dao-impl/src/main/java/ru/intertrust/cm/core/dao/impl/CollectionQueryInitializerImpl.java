package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl.PARAM_NAME_PREFIX;
import static ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl.adjustParameterNamesBeforePreProcessing;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getFilterParameterPrefix;
import static ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier.transformToCountQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.CollectionPlaceholderConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryParser;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Инициализирует запрос для извлечения коллекций, заполняет параметры в
 * конфигурации фильтров, устанавливает порядок сортировки
 * @author atsvetkov
 * 
 */
public class CollectionQueryInitializerImpl implements CollectionQueryInitializer {

    private static final String PLACEHOLDER_PREFIX = "::";

    private static final String EMPTY_STRING = " ";

    private static final String SQL_DESCENDING_ORDER = "desc";

    private static final String SQL_ASCENDING_ORDER = "asc";

    public static final String DEFAULT_CRITERIA_CONDITION = "and";

    private final ConfigurationExplorer configurationExplorer;
    private final UserGroupGlobalCache userGroupCache;
    private final CurrentUserAccessor currentUserAccessor;
    private final DomainObjectQueryHelper domainObjectQueryHelper;

    public CollectionQueryInitializerImpl(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache,
            CurrentUserAccessor currentUserAccessor, DomainObjectQueryHelper domainObjectQueryHelper) {
        this.configurationExplorer = configurationExplorer;
        this.userGroupCache = userGroupCache;
        this.currentUserAccessor = currentUserAccessor;
        this.domainObjectQueryHelper = domainObjectQueryHelper;
    }

    /**
     * Применение фильтров, сортировки и т.д. к прототипу запроса.
     * @param filterValues
     *            заполненные фильтры
     * @param sortOrder
     *            порядок сортировки
     * @param offset
     *            смещение
     * @param limit
     *            ограничение количества
     */
    @Override
    public String initializeQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        String prototypeQuery = collectionConfig.getPrototype();

        String filledQuery = fillPrototypeQuery(filledFilterConfigs, prototypeQuery);

        filledQuery = processPersonParameter(filledQuery);
        filledQuery = adjustParameterNamesBeforePreProcessing(filledQuery, PARAM_NAME_PREFIX);

        filledQuery = postProcessQuery(collectionConfig, filterValues, sortOrder, offset, limit, accessToken, filledQuery);

        return filledQuery;
    }

    @Override
    public String initializeQuery(String query, int offset, int limit, AccessToken accessToken) {
        return postProcessQuery(accessToken, query, offset, limit);
    }

    /**
     * Применение фильтров, и т.д. к прототипу запроса на количество доменных
     * объектов в коллекции.
     * @param filterValues
     *            заполненные фильтры
     */
    @Override
    public String initializeCountQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues,
            AccessToken accessToken) {
        String prototypeQuery = collectionConfig.getCountingPrototype();
        if (prototypeQuery != null) {
            List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);
            String filledQuery = fillPrototypeQuery(filledFilterConfigs, prototypeQuery);
            filledQuery = processPersonParameter(filledQuery);
            filledQuery = adjustParameterNamesBeforePreProcessing(filledQuery, PARAM_NAME_PREFIX);
            filledQuery = postProcessQuery(collectionConfig, filterValues, accessToken, filledQuery);
            return filledQuery;
        } else {
            String filledQuery = initializeQuery(collectionConfig, filterValues, null, 0, 0, accessToken);
            return transformToCountQuery(filledQuery);
        }
    }

    protected String applyOffsetAndLimit(String query, int offset, int limit) {
        StringBuilder collectionQuery = new StringBuilder(query);
        DaoUtils.applyOffsetAndLimit(collectionQuery, offset, limit);
        return collectionQuery.toString();
    }

    /**
     * Заполняет конфигурации фильтров значениями. Возвращает заполненные
     * конфигурации фильтров (для которых были переданы значения). Сделан
     * публичным для тестов.
     */
    private List<CollectionFilterConfig> findFilledFilterConfigs(List<? extends Filter> filterValues,
            CollectionConfig collectionConfig) {
        List<CollectionFilterConfig> filterConfigs = collectionConfig.getFilters();

        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();

        if (filterConfigs == null || filterValues == null) {
            return filledFilterConfigs;
        }

        for (CollectionFilterConfig filterConfig : filterConfigs) {
            for (Filter filterValue : filterValues) {
                if (!filterConfig.getName().equals(filterValue.getFilter())) {
                    continue;
                }
                CollectionFilterConfig filledFilterConfig = replaceFilterParam(filterConfig, filterValue);
                filledFilterConfigs.add(filledFilterConfig);
            }
        }
        return filledFilterConfigs;
    }

    private CollectionFilterConfig replaceFilterParam(CollectionFilterConfig filterConfig, Filter filterValue) {
        CollectionFilterConfig clonedFilterConfig = cloneFilterConfig(filterConfig);

        String filterName = filterValue.getFilter();
        String parameterPrefix = getFilterParameterPrefix(filterName);

        if (clonedFilterConfig.getFilterCriteria() != null) {
            String criteria = clonedFilterConfig.getFilterCriteria().getValue();
            String newFilterCriteria = CollectionsDaoImpl.adjustParameterNames(criteria, parameterPrefix);
            clonedFilterConfig.getFilterCriteria().setValue(newFilterCriteria);
        }

        if (clonedFilterConfig.getFilterReference() != null) {
            String reference = clonedFilterConfig.getFilterReference().getValue();
            String newFilterReference = CollectionsDaoImpl.adjustParameterNames(reference, parameterPrefix);
            clonedFilterConfig.getFilterReference().setValue(newFilterReference);
        }

        return clonedFilterConfig;
    }

    /**
     * Клонирует конфигурацию коллекции. При заполнении параметров в фильтрах
     * нужно, чтобы первоначальная конфигурация коллекции оставалась неизменной.
     * @param filterConfig
     *            конфигурации коллекции
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
            String parameterPrefix = PARAM_NAME_PREFIX;
            filledQuery = CollectionsDaoImpl.adjustParameterNames(filledQuery, parameterPrefix);
        }
        return filledQuery;
    }

    private String postProcessQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues, AccessToken accessToken, String query) {
        return postProcessQuery(collectionConfig, filterValues, null, 0, 0, accessToken, query);
    }

    /**
     * Пост обработка запроса после применения фильтров и правил сортировки.
     * Добавляет поле тип идентификатора доменного объекта и ACL фильтр.
     * @param collectionConfig
     *            конфигурация коллекции
     * @param accessToken
     *            маркер доступа. В случае отложенного маркера добавляет ACL
     *            фильтр.
     * @param query
     *            первоначальный запрос
     * @return измененный запрос
     */
    private String postProcessQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken, String query) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        Select select = sqlParser.getSelectStatement();

        SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();
        sqlQueryModifier.addServiceColumns(select);

        SelectBody selectBody = select.getSelectBody();
        selectBody = sqlQueryModifier.addIdBasedFilters(selectBody, filterValues, collectionConfig.getIdField());
        select.setSelectBody(selectBody);

        if (accessToken.isDeferred()) {
            sqlQueryModifier.addAclQuery(select);
        }

        sqlQueryModifier.checkDuplicatedColumns(select);

        query = applySortOrder(sortOrder, select);

        return applyOffsetAndLimit(query, offset, limit);
    }

    /**
     * Пост обработка запроса после применения фильтров и правил сортировки.
     * Добавляет поле тип идентификатора доменного объекта и ACL фильтр.
     * @param accessToken
     *            маркер доступа. В случае отложенного маркера добавляет ACL
     *            фильтр.
     * @param query
     *            первоначальный запрос
     * @return измененный запрос
     */
    private String postProcessQuery(AccessToken accessToken, String query, int offset, int limit) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        Select select = sqlParser.getSelectStatement();

        SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();
        sqlQueryModifier.addServiceColumns(select);

        if (accessToken.isDeferred()) {
            sqlQueryModifier.addAclQuery(select);
        }

        sqlQueryModifier.checkDuplicatedColumns(select);

        query = applyOffsetAndLimit(sqlParser.toString(), offset, limit);
        return query;
    }

    private SqlQueryModifier createSqlQueryModifier() {
        return new SqlQueryModifier(configurationExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
    }

    private String fillPrototypeQuery(List<CollectionFilterConfig> filledFilterConfigs,
            String prototypeQuery) {
        if (prototypeQuery == null || prototypeQuery.trim().length() == 0) {
            throw new FatalException("Prototype query is null and can not be processed");
        }
        return mergeFilledFilterConfigsInPrototypeQuery(prototypeQuery, filledFilterConfigs);
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
            checkPlaceholderExist(PLACEHOLDER_PREFIX + placeholder, prototypeQuery);
            prototypeQuery = prototypeQuery.replace(PLACEHOLDER_PREFIX + placeholder, placeholderValue);
        }

        for (String placeholder : criteriaPlaceHolderCollector.getPlaceholders()) {
            String placeholderValue = criteriaPlaceHolderCollector.getPlaceholderValue(placeholder);
            if (placeholderValue == null) {
                placeholderValue = EMPTY_STRING;
            }

            checkPlaceholderExist(PLACEHOLDER_PREFIX + placeholder, prototypeQuery);
            prototypeQuery = prototypeQuery.replace(PLACEHOLDER_PREFIX + placeholder, placeholderValue);
        }

        prototypeQuery = applyGlobalPlaceholders(prototypeQuery);

        return prototypeQuery;
    }

    /**
     * Проверяет наличие placeholder в теле основного запроса если отсутствует -
     * выбрасываем исключение
     */
    private void checkPlaceholderExist(String placeholder, String prototypeQuery) {
        if (!prototypeQuery.contains(placeholder)) {
            throw new CollectionQueryException("The collection query \"" + prototypeQuery +
                    "\" has no placeholder \"" + placeholder + "\"");
        }
    }

    /**
     * Не заполненные placeholders ищет в глобальных настройках, если не находит удаляет из запроса
     * @param prototypeQuery
     *            исходный запрос
     * @return измененный запрос
     */
    private String applyGlobalPlaceholders(String prototypeQuery) {
        Pattern pattern = Pattern.compile("::(?!timestamp)[\\w\\d_\\-]+");
        Matcher matcher = pattern.matcher(prototypeQuery);
        String result = prototypeQuery;
        while (matcher.find()) {
            String placeholderName = matcher.group().substring(2);
            CollectionPlaceholderConfig collectionPlaceholder = configurationExplorer.getConfig(CollectionPlaceholderConfig.class, placeholderName);
            if (collectionPlaceholder != null) {
                result = result.replaceAll(matcher.group(), collectionPlaceholder.getBody());
            } else {
                result = result.replaceAll(matcher.group(), "");
            }
        }
        return result;
    }

    private String applySortOrder(SortOrder sortOrder, Select select) {
        StringBuilder prototypeQuery = new StringBuilder(select.toString());
        boolean hasSortEntry = false;
        if (sortOrder != null && sortOrder.size() > 0) {
            select = clearOrderByExpression(select);
            prototypeQuery = new StringBuilder(select.toString());

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

    private Select clearOrderByExpression(Select select) {
        SelectBody selectBody = select.getSelectBody();

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            if (plainSelect.getOrderByElements() != null && plainSelect.getOrderByElements().size() > 0) {
                plainSelect.getOrderByElements().clear();
            }
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList union = (SetOperationList) selectBody;
            if (union.getOrderByElements() != null && union.getOrderByElements().size() > 0) {
                union.getOrderByElements().clear();
            }
            List<?> plainSelects = union.getSelects();
            for (Object subSelect : plainSelects) {
                if (subSelect instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) subSelect;
                    if (plainSelect.getOrderByElements() != null && plainSelect.getOrderByElements().size() > 0) {
                        plainSelect.getOrderByElements().clear();
                    }
                }
            }
        }
        return select;
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

        private final Map<String, String> placeholdersMap = new HashMap<>();

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
     * Группирует все фильтры после слова where по названию placeholder. Т.е.
     * для каждого placeholder составляет запрос из заполненных фильтров. По
     * умолчанию все фильтры соединяются через условие AND (
     * {@link CollectionQueryInitializerImpl#DEFAULT_CRITERIA_CONDITION})
     * @author atsvetkov
     */
    private class CriteriaPlaceHolderCollector {

        private final Map<String, String> placeholdersMap = new HashMap<>();

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
            return EMPTY_STRING + condition + EMPTY_STRING + "(" + value + ")";
        }

        public String getPlaceholderValue(String placeholder) {
            return placeholdersMap.get(placeholder);
        }

        public Set<String> getPlaceholders() {
            return placeholdersMap.keySet();
        }
    }

}
