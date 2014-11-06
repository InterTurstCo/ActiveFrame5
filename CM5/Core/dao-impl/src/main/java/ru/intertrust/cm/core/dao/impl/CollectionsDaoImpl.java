package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier.wrapAndLowerCaseNames;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.setParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdsExcludedFilter;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ServerComponentService;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.exception.CollectionConfigurationException;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.utils.CollectionRowMapper;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:58 PM
 */
public class CollectionsDaoImpl implements CollectionsDao {

    public static final String END_PARAM_SIGN = "_END_PARAM_'";
    public static final String START_PARAM_SIGN = "'_START_PARAM";
    public static final String PARAM_NAME_PREFIX = "_PARAM_NAME_";
    public static final String CURRENT_PERSON_PARAM = "CURRENT_PERSON";


    private static final String PARAM_NAME_PREFIX_SPRING = ":";

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private CollectionsCacheServiceImpl collectionsCacheService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private ServerComponentService serverComponentService;
    
    public void setJdbcTemplate(NamedParameterJdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    public void setCollectionsCacheService(CollectionsCacheServiceImpl collectionsCacheService) {
        this.collectionsCacheService = collectionsCacheService;
    }

    /**
     * Устанавливает {@link #configurationExplorer}
     *
     * @param configurationExplorer {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /*
     * {@see
     * ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollection(ru.intertrust.cm.core.config.model.CollectionNestedConfig,
     * java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder, int, int)}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName,
                                                       List<? extends Filter> filterValues,
                                                       SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {

        checkFilterValues(filterValues);

        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);

        if (collectionConfig.getTransactionCache() == CollectionConfig.TransactionCacheType.enabled){
            IdentifiableObjectCollection fromCache = collectionsCacheService.getCollectionFromCache(collectionName, filterValues, sortOrder, offset, limit);
            if (fromCache != null) {
                return fromCache;
            }
        }

        if (collectionConfig.getGenerator() != null) {
            String collectionGeneratorComponent = collectionConfig.getGenerator().getClassName();
            return getCollectionFromGenerator(collectionGeneratorComponent, filterValues, sortOrder, offset, limit);
        }
        
        String collectionQuery =
                getFindCollectionQuery(collectionConfig, filterValues, sortOrder, offset, limit, accessToken);

        Map<String, FieldConfig> columnToConfigMap =
                new SqlQueryModifier(configurationExplorer).buildColumnToConfigMapForParameters(collectionQuery);
        Map<String, FieldConfig> columnToConfigMapForSelectItems = new SqlQueryModifier(configurationExplorer).buildColumnToConfigMapForSelectItems(collectionQuery);

        
        SqlQueryModifier sqlQueryModifier = new SqlQueryModifier(configurationExplorer);
        collectionQuery = sqlQueryModifier.modifyQueryWithReferenceFilterValues(collectionQuery, filterValues, columnToConfigMap);

        Map<String, Object> parameters = new HashMap<>();
        fillFilterParameters(filterValues, parameters);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        addCurrentPersonParameter(collectionQuery, parameters);
        collectionQuery = adjustParameterNamesForSpring(collectionQuery);
        collectionQuery = wrapAndLowerCaseNames(collectionQuery);


        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(collectionName, columnToConfigMapForSelectItems, collectionConfig.getIdField(),
                        configurationExplorer, domainObjectTypeIdCache));

        if (collectionConfig.getTransactionCache() == CollectionConfig.TransactionCacheType.enabled){
            collectionsCacheService.putCollectionToCache(collection, collectionName, filterValues, sortOrder, offset, limit);
        }

        return collection;
    }

    private IdentifiableObjectCollection getCollectionFromGenerator(String collectionGeneratorComponent, List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit) {
        CollectionDataGenerator collectionDataGenerator = (CollectionDataGenerator) serverComponentService.getServerComponent(collectionGeneratorComponent);
        return collectionDataGenerator.findCollection(filterValues, sortOrder, offset, limit);
    }

    /**
     * При запросе коллекции с набором фильтров, если в этом наборе дважды
     * встречается фильтр с одним и тем же именем, следует выбрасывать
     * исключение IllegalArgumentException.
     * @param filterValues набор фильтров
     */
    private void checkFilterValues(List<? extends Filter> filterValues) {
        if (filterValues != null && !filterValues.isEmpty()) {
            List <String> names = new ArrayList<>();
            for (Filter filterValue : filterValues) {
                String filterName = filterValue.getFilter();
                if (names.contains(filterName)) {
                    throw new IllegalArgumentException("Filter values have duplicate filter name:" + filterName);
                }
                names.add(filterName);
            }
        }
    }

    private void addCurrentPersonParameter(String collectionQuery, Map<String, Object> parameters) {
        if (collectionQuery.indexOf(CURRENT_PERSON_PARAM) > 0) {
            Id personId = getCurrentUserId();
            parameters.put(CURRENT_PERSON_PARAM, ((RdbmsId)personId).getId());
        }
    }

    private Id getCurrentUserId() {
        return currentUserAccessor.getCurrentUserId();
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit,
                                                              AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);
        String collectionQuery = collectionQueryInitializer.initializeQuery(query, offset, limit, accessToken);

        Map<String, Object> parameters = new HashMap<>();
        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        Map<String, FieldConfig> columnToConfigMapForSelectItems =
                new SqlQueryModifier(configurationExplorer).buildColumnToConfigMapForSelectItems(collectionQuery);

        collectionQuery = wrapAndLowerCaseNames(collectionQuery);

        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(columnToConfigMapForSelectItems, configurationExplorer, domainObjectTypeIdCache));

        return collection;
    }


    /*
     * {@inheritDoc}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params,
            int offset, int limit, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

        String collectionQuery = adjustParameterNamesBeforePreProcessing(query, CollectionsDaoImpl.PARAM_NAME_PREFIX);
        collectionQuery = collectionQueryInitializer.initializeQuery(collectionQuery, offset, limit, accessToken);

        Map<String, Object> parameters = new HashMap<>();

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        SqlQueryModifier sqlQueryModifier = new SqlQueryModifier(configurationExplorer);

        Map<String, FieldConfig> columnToConfigMap = sqlQueryModifier.buildColumnToConfigMapForParameters(collectionQuery);
        Map<String, FieldConfig> columnToConfigMapForSelectItems = sqlQueryModifier.buildColumnToConfigMapForSelectItems(collectionQuery);

        collectionQuery = sqlQueryModifier.modifyQueryWithParameters(collectionQuery, params, columnToConfigMap);
        collectionQuery = wrapAndLowerCaseNames(collectionQuery);
        collectionQuery = adjustParameterNamesAfterPreProcessing(collectionQuery);

        fillParameterMap(params, parameters);

        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(columnToConfigMapForSelectItems, configurationExplorer, domainObjectTypeIdCache));

        return collection;
    }

    public static String adjustParameterNames(String subQuery, String parameterPrefix) {
        String newFilterCriteria = subQuery.replace("{", parameterPrefix);
        newFilterCriteria = newFilterCriteria.replace("}", "");

        return newFilterCriteria;
    }

    /**
     *
     * @param subQuery
     * @param parameterPrefix
     * @return
     */
    public static String adjustParameterNamesBeforePreProcessing(String subQuery, String parameterPrefix) {
        String newFilterCriteria = subQuery.replaceAll("[{]", START_PARAM_SIGN + parameterPrefix);
        newFilterCriteria = newFilterCriteria.replaceAll("[}]", END_PARAM_SIGN);
        return newFilterCriteria;
    }

    /*
     * {@see ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollectionCountByQuery(ru.intertrust.cm.core.config.model.
     * CollectionNestedConfig , java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder)}
     */
    @Override
    public int findCollectionCount(String collectionName,
            List<? extends Filter> filterValues, AccessToken accessToken) {
        checkFilterValues(filterValues);

        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);

        if (collectionConfig.getGenerator() != null) {
            String collectionGeneratorComponent = collectionConfig.getGenerator().getClassName();
            CollectionDataGenerator collectionDataGenerator = (CollectionDataGenerator) serverComponentService.getServerComponent(collectionGeneratorComponent);
            return collectionDataGenerator.findCollectionCount(filterValues);
        }

        String collectionQuery = getFindCollectionCountQuery(collectionConfig, filterValues, accessToken);

        Map<String, Object> parameters = new HashMap<String, Object>();
        fillFilterParameters(filterValues, parameters);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        collectionQuery = adjustParameterNamesForSpring(collectionQuery);
        collectionQuery = wrapAndLowerCaseNames(collectionQuery);

        return jdbcTemplate.queryForObject(collectionQuery, parameters, Integer.class);
    }

    protected CollectionQueryInitializer createCollectionQueryInitializer(ConfigurationExplorer configurationExplorer) {
        return new CollectionQueryInitializerImpl(configurationExplorer);
    }

    /**
     * Возвращает запрос, который используется в методе поиска коллекции доменных объектов
     *
     * @param collectionConfig
     * @param filterValues
     * @param sortOrder
     * @param offset
     * @param limit
     * @return
     */
    protected String getFindCollectionQuery(CollectionConfig collectionConfig,
                                            List<? extends Filter> filterValues, SortOrder sortOrder,
                                            int offset, int limit, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

        String collectionQuery = collectionQueryInitializer.initializeQuery(collectionConfig, filterValues,
                sortOrder, offset, limit, accessToken);

        return collectionQuery;
    }

    /**
     * Возвращает запрос, который используется в методе поиска количества объектов в коллекции
     *
     * @param collectionConfig
     * @param filterValues
     * @return
     */
    protected String getFindCollectionCountQuery(CollectionConfig collectionConfig,
                                                 List<? extends Filter> filterValues, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

        String collectionQuery =
                collectionQueryInitializer.initializeCountQuery(collectionConfig, filterValues, accessToken);

        return collectionQuery;
    }

    private void fillParameterMap(List<? extends Value> params, Map<String, Object> parameterMap) {
        int index = 0;
        for (Value value : params) {
            // ссылочные параметры не добавляются в список параметров, т.к. их значения подставляются прямо в запрос
            if (!(value instanceof ReferenceValue)) {
                // в исполняемом SQL запросе названия параметров совпадает с их номерами в начальном SQL запросе.
                String parameterName = index + "";
                setParameter(parameterName, value, parameterMap);
            }
            index++;
        }
    }
    private void fillAclParameters(AccessToken accessToken, Map<String, Object> parameters) {
        parameters.put("user_id", ((UserSubject)accessToken.getSubject()).getUserId());
    }

    /**
     * Модифицирует имена параметров в названия совместимые с {@see NamedParameterJdbcTemplate}. Заменяет префикс
     * "_PARAM_NAME_" на ":"
     * @param collectionQuery SQL запрос
     * @return
     */
    private String adjustParameterNamesForSpring(String collectionQuery) {
        collectionQuery = collectionQuery.replaceAll(PARAM_NAME_PREFIX, PARAM_NAME_PREFIX_SPRING);
        return collectionQuery;
    }

    private String adjustParameterNamesAfterPreProcessing(String collectionQuery) {
        collectionQuery = collectionQuery.replaceAll(START_PARAM_SIGN, "");
        collectionQuery = collectionQuery.replaceAll(END_PARAM_SIGN, "");

        collectionQuery = adjustParameterNamesForSpring(collectionQuery);
        return collectionQuery;
    }

    /**
     * Заполняет параметры. Имена параметров в формате имя_фильтра + ключ парметра, указанный в конфигурации.
     *
     * @param filterValues
     * @param parameters
     */
    private void fillFilterParameters(List<? extends Filter> filterValues, Map<String, Object> parameters) {
        if (filterValues != null) {
            for (Filter filter : filterValues) {
                for (Integer key : filter.getCriterionKeys()) {
                    String parameterName = filter.getFilter() + key;

                    Object criterion = getFilterCriterion(filter, key);

                    if(criterion == null){
                        throw new CollectionConfigurationException("Not Criterion nor MultiCriterion is defined for filter");
                    }

                    if (criterion instanceof Value) {
                        setParameter(parameterName, (Value) criterion, parameters);
                    } else if (criterion instanceof List) {
                        List<Value> valuesList = (List) criterion;
                        List<Object> parameterValues = getParameterValues(valuesList);
                        parameters.put(parameterName, parameterValues);
                    }

                    if (filter instanceof IdsIncludedFilter || filter instanceof IdsExcludedFilter) {
                        parameterName = filter.getFilter() + key + REFERENCE_TYPE_POSTFIX;
                        ReferenceValue referenceValue = (ReferenceValue) criterion;
                        parameters.put(parameterName, ((RdbmsId) referenceValue.get()).getTypeId());
                    }
                }
            }
        }
    }


    private List<Object> getParameterValues(List<Value> valuesList) {
        List<Object> parameterValues = new ArrayList<Object>();

        for (Value value : valuesList) {
            Object parameterValue = getParameterValue(value);
            parameterValues.add(parameterValue);

        }
        return parameterValues;
    }


    private Object getFilterCriterion(Filter filter, Integer key) {
        Object criterion = filter.getCriterion(key);
        if(criterion == null){
            criterion = filter.getMultiCriterion(key);
        }
        return criterion;
    }


    private Object getParameterValue(Value value) {
        Object parametrValue = null;
        if (value instanceof ReferenceValue) {
            parametrValue = ((RdbmsId) value.get()).getId();
        } else {
            parametrValue = value.get();
        }
        return parametrValue;
    }
}
