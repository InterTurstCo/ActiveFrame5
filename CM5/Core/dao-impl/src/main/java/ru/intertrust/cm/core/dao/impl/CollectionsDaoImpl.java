package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.impl.utils.CollectionRowMapper;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:58 PM
 */
public class CollectionsDaoImpl implements CollectionsDao {

    private static final String PARAM_NAME_PREFIX = "_PARAM_NAME_";

    private static final String PARAM_NAME_PREFIX_SPRING = ":";

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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
     * ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollectionByQuery(ru.intertrust.cm.core.config.model.CollectionNestedConfig,
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
        
        collectionQuery = adjustParameterNamesForSpring(collectionQuery);
        
        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(collectionName, collectionConfig.getIdField(), configurationExplorer));

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
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer();
        
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
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer();

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
                    Value value = filter.getCriterion(key);
                    parameters.put(parameterName, value.get());
                }
            }
        }
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
     * Заменяет названия параметров в конфигурации фильтра по схеме {0} - > ":filterName" + 0.
     * @param filterConfig
     * @param filterValue
     * @return
     */
    private CollectionFilterConfig replaceFilterCriteriaParam(CollectionFilterConfig filterConfig, Filter filterValue) {
        CollectionFilterConfig clonedFilterConfig = cloneFilterConfig(filterConfig);

        String criteria = clonedFilterConfig.getFilterCriteria().getValue();
        String parameterPrefix = PARAM_NAME_PREFIX + filterValue.getFilter();
        String newFilterCriteria = criteria.replaceAll("[{]", parameterPrefix);
        newFilterCriteria = newFilterCriteria.replaceAll("[}]", "");
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
}
