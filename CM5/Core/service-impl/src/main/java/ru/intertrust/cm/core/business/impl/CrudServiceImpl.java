package ru.intertrust.cm.core.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.CollectionConfig;
import ru.intertrust.cm.core.config.CollectionConfiguration;
import ru.intertrust.cm.core.config.CollectionFilterConfig;
import ru.intertrust.cm.core.config.CollectionFilterCriteria;
import ru.intertrust.cm.core.config.CollectionFilterReference;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;

/**
 * Реализация сервиса для работы c базовыvb CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 *
 */
public class CrudServiceImpl implements CrudService {

    private static final String EMPTY_PLACEHOLDER = " ";

    private static final String CRITERIA_PLACEHOLDER = "::where-clause";

    private static final String REFERENCE_PLACEHOLDER = "::from-clause";

    private static final String SQL_DESCENDING_ORDER = "desc";

    private static final String SQL_ASCENDING_ORDER = "asc";

    public static final String QUERY_FILTER_PARAM_DELIMETER = ":";
    
    public static final String DEFAULT_CRITERIA_CONDITION = "and";

    private ConfigurationLoader loader;

    private CrudServiceDAO crudServiceDAO;

    public void setCrudServiceDAO(CrudServiceDAO crudServiceDAO) {
        this.crudServiceDAO = crudServiceDAO;
    }

    public void setLoader(ConfigurationLoader loader) {
        this.loader = loader;
    }

    @Override
    public IdentifiableObject createIdentifiableObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BusinessObject createBusinessObject(String name) {

        // TODO Auto-generated method stub
        return null;
    }

    protected BusinessObject create(BusinessObject businessObject) {
        Id id = businessObject.getId();
        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigById(
                loader.getConfiguration(), businessObject.getId());
        businessObject.setCreatedDate(new Date());
        return crudServiceDAO.create(businessObject, businessObjectConfig);

    }

    protected BusinessObject update(BusinessObject businessObject) {

        Id id = businessObject.getId();
        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigById(
                loader.getConfiguration(), id);

        return crudServiceDAO.update(businessObject, businessObjectConfig);

    }

    @Override
    public BusinessObject save(BusinessObject businessObject) {

        if (businessObject.getCreatedDate() == null) {
            return create(businessObject);
        }

        return update(businessObject);

    }

    @Override
    public List<BusinessObject> save(List<BusinessObject> businessObjects) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean exists(Id id) {
        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigById(
                loader.getConfiguration(), id);

        return crudServiceDAO.exists(id, businessObjectConfig);
    }

    @Override
    public BusinessObject find(Id id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<BusinessObject> find(List<Id> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filterValues, SortOrder sortOrder, int offset, int limit) {
        
        CollectionConfiguration collectionsConfiguration = loader.getCollectionConfiguration();
        
        if(collectionsConfiguration == null){
            new RuntimeException ("Collection configuration is not loaded");
        }
        
        CollectionConfig collectionConfig = collectionsConfiguration.findCollectionConfigByName(collectionName);

        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        String prototypeQuery = mergeFilledFilterConfigsInPrototypeQuery(collectionConfig.getPrototype(), filledFilterConfigs);
        
        prototypeQuery = applySortOrder(sortOrder, prototypeQuery);        
        
        return crudServiceDAO.findCollectionByQuery(prototypeQuery, collectionConfig.getBusinessObjectTypeField(), collectionConfig.getIdField(), 0, 0);              
    }

    private List<CollectionFilterConfig> findFilledFilterConfigs(List<Filter> filterValues, CollectionConfig collectionConfig) {
        List<CollectionFilterConfig> filterConfigs = collectionConfig.getFilters();

        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<CollectionFilterConfig>();

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
            prototypeQuery = prototypeQuery.replaceAll(CRITERIA_PLACEHOLDER, EMPTY_PLACEHOLDER);
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

    private CollectionFilterConfig replaceFilterCriteriaParam(CollectionFilterConfig filterConfig, Filter filterValue) {
        CollectionFilterConfig clonedFilterConfig = cloneFilterConfig(filterConfig);
        int index = 0;

        for (String value : filterValue.getValues()) {

            String criteria = clonedFilterConfig.getFilterCriteria().getValue();

            String paramName = ":" + index;

            String newFilterCriteria = criteria.replaceAll(paramName, value);

            clonedFilterConfig.getFilterCriteria().setValue(newFilterCriteria);
            index++;

        }

        return clonedFilterConfig;
    }

    /**
     * Клонирует конфигурацию коллекции. При заполнении параметров в фильтрах нужно, чтобы первоначальная конфигурация коллекции оставалась неизменной.
     * @param filterConfig конфигурации коллекции
     * @return копия переданной конфигурации коллекции
     */
    private CollectionFilterConfig cloneFilterConfig(CollectionFilterConfig filterConfig) {
        CollectionFilterConfig clonedFilterConfig = new CollectionFilterConfig();

        CollectionFilterReference srcFilterReference = filterConfig.getFilterReference();
        if (srcFilterReference != null) {
            CollectionFilterReference clonedFilterReference = new CollectionFilterReference();
            clonedFilterReference.setPlaceholder(srcFilterReference.getPlaceholder());
            clonedFilterReference.setValue(srcFilterReference.getValue());
            clonedFilterConfig.setFilterReference(clonedFilterReference);
        }

        CollectionFilterCriteria srcFilterCriteria = filterConfig.getFilterCriteria();
        if (srcFilterCriteria != null) {
            CollectionFilterCriteria clonedFilterCriteria = new CollectionFilterCriteria();
            clonedFilterCriteria.setPlaceholder(srcFilterCriteria.getPlaceholder());
            clonedFilterCriteria.setCondition(srcFilterCriteria.getCondition());
            clonedFilterCriteria.setValue(srcFilterCriteria.getValue());
            clonedFilterConfig.setFilterCriteria(clonedFilterCriteria);
        }

        clonedFilterConfig.setName(filterConfig.getName());

        return clonedFilterConfig;
    }
    
    @Override
    public int findCollectionCount(String collectionName, List<Filter> filters, SortOrder sortOrder) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void delete(Id id) {

        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigById(
                loader.getConfiguration(), id);

        crudServiceDAO.delete(id, businessObjectConfig);

    }

    @Override
    public int delete(Collection<Id> ids) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int deleteAll(String businessObjectName) {
        // TODO Auto-generated method stub
        return 0;
    }

}
