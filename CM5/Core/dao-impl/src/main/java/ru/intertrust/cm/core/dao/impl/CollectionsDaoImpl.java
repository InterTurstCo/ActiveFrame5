package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.model.base.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.model.base.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.exception.CollectionConfigurationException;
import ru.intertrust.cm.core.dao.impl.utils.CollectionRowMapper;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:58 PM
 */
public class CollectionsDaoImpl implements CollectionsDao {

    public static final String PARAM_NAME_PREFIX = "_PARAM_NAME_";
    public static final String CURRENT_PERSON_PARAM = "CURRENT_PERSON";
    

    private static final String PARAM_NAME_PREFIX_SPRING = ":";
    
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired    
    private CurrentUserAccessor currentUserAccessor; 
    
    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
    }

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
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
                                                       List<Filter> filterValues,
                                                       SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {

        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        String collectionQuery =
                getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, offset, limit, accessToken);

        Map<String, Object> parameters = new HashMap<>();
        fillFilterParameters(filterValues, parameters);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        addCurrentPersonParameter(collectionQuery, parameters);
        
        collectionQuery = adjustParameterNamesForSpring(collectionQuery);
        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(collectionName, collectionConfig.getIdField(), configurationExplorer,
                        domainObjectTypeIdCache));

        return collection;
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
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer(configurationExplorer);
        String collectionQuery = collectionQueryInitializer.initializeQuery(query, offset, limit, accessToken);

        Map<String, Object> parameters = new HashMap<>();
        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        collectionQuery = adjustParameterNamesForSpring(collectionQuery);

        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(configurationExplorer, domainObjectTypeIdCache));

        return collection;
    }


    private void fillAclParameters(AccessToken accessToken, Map<String, Object> parameters) {
        parameters.put("user_id", ((UserSubject)accessToken.getSubject()).getUserId());
    }

    /*
     * {@see ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollectionCountByQuery(ru.intertrust.cm.core.config.model.
     * CollectionNestedConfig , java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder)}
     */
    @Override
    public int findCollectionCount(String collectionName,
            List<Filter> filterValues, AccessToken accessToken) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);
        String collectionQuery = getFindCollectionCountQuery(collectionConfig, filledFilterConfigs, accessToken);

        Map<String, Object> parameters = new HashMap<String, Object>();
        fillFilterParameters(filterValues, parameters);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }
        collectionQuery = adjustParameterNamesForSpring(collectionQuery);

        return jdbcTemplate.queryForObject(collectionQuery, parameters, Integer.class);
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

    /**
     * Возвращает запрос, который используется в методе поиска коллекции доменных объектов
     *
     * @param collectionConfig
     * @param filledFilterConfigs
     * @param sortOrder
     * @param offset
     * @param limit
     * @return
     */
    protected String getFindCollectionQuery(CollectionConfig collectionConfig,
                                            List<CollectionFilterConfig> filledFilterConfigs, SortOrder sortOrder,
                                            int offset, int limit, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer(configurationExplorer);

        String collectionQuery = collectionQueryInitializer.initializeQuery(collectionConfig, filledFilterConfigs,
                        sortOrder, offset, limit, accessToken);

        return collectionQuery;
    }

    /**
     * Возвращает запрос, который используется в методе поиска количества объектов в коллекции
     *
     * @param collectionConfig
     * @param filledFilterConfigs
     * @return
     */
    protected String getFindCollectionCountQuery(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer(configurationExplorer);

        String collectionQuery =
                collectionQueryInitializer.initializeCountQuery(collectionConfig, filledFilterConfigs, accessToken);

        return collectionQuery;
    }

    /**
     * Заполняет параметры. Имена параметров в формате имя_фильтра + ключ парметра, указанный в конфигурации.
     *
     * @param filterValues
     * @param parameters
     */
    private void fillFilterParameters(List<Filter> filterValues, Map<String, Object> parameters) {
        if (filterValues != null) {
            for (Filter filter : filterValues) {
                for (Integer key : filter.getCriterionKeys()) {
                    String parameterName = filter.getFilter() + key;
                    
                    Object criterion = getFilterCriterion(filter, key);
                    
                    if(criterion == null){
                        throw new CollectionConfigurationException("Not Criterion nor MultiCriterion is defined for filter");
                    }
                    if (criterion instanceof Value) {
                        Value value = (Value) criterion;
                        Object parameterValue = getParameterValue(value);

                        parameters.put(parameterName, parameterValue);

                    } else if (criterion instanceof List) {
                        List<Value> valuesList = (List) criterion;                        
                        List<Object> parameterValues = getParameterValues(valuesList);
                        parameters.put(parameterName, parameterValues);
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

    /**
     * Заполняет конфигурации фильтров значениями. Возвращает заполненные конфигурации фильтров (для которых были
     * переданы значения). Сделан публичным для тестов.
     * @param filterValues
     * @param collectionConfig
     * @return
     */
    private List<CollectionFilterConfig> findFilledFilterConfigs(List<Filter> filterValues,
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

    /**
     * Заменяет названия параметров в конфигурации фильтра по схеме {0} - > ":filterName_0".
     * @param filterConfig
     * @param filterValue
     * @return
     */
    private CollectionFilterConfig replaceFilterCriteriaParam(CollectionFilterConfig filterConfig, Filter filterValue) {
        CollectionFilterConfig clonedFilterConfig = cloneFilterConfig(filterConfig);

        String criteria = clonedFilterConfig.getFilterCriteria().getValue();
        String filterName = filterValue.getFilter();
               
        String parameterPrefix = PARAM_NAME_PREFIX + filterName;
        String newFilterCriteria = adjustParameterNames(criteria, parameterPrefix);

        clonedFilterConfig.getFilterCriteria().setValue(newFilterCriteria);
        return clonedFilterConfig;
    }

    public static String adjustParameterNames(String subQuery, String parameterPrefix) {
        String newFilterCriteria = subQuery.replaceAll("[{]", parameterPrefix);
        newFilterCriteria = newFilterCriteria.replaceAll("[}]", "");
        return newFilterCriteria;
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
}
