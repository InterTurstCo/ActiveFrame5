package ru.intertrust.cm.core.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericBusinessObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.CollectionConfig;
import ru.intertrust.cm.core.config.CollectionConfiguration;
import ru.intertrust.cm.core.config.CollectionFilterConfig;
import ru.intertrust.cm.core.config.CollectionFilterCriteria;
import ru.intertrust.cm.core.config.CollectionFilterReference;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;

/**
 * Реализация сервиса для работы c базовыvb CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 *
 */
public class CrudServiceImpl implements CrudService {

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

        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigByName(
                loader.getConfiguration(), name);

        BusinessObject businessObject = new GenericBusinessObject();
        businessObject.setTypeName(name);
        Date currentDate = new Date();
        businessObject.setCreatedDate(currentDate);
        businessObject.setModifiedDate(currentDate);

        return businessObject;
    }

    protected BusinessObject create(BusinessObject businessObject) {

        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigByName(
                loader.getConfiguration(), businessObject.getTypeName());
        Date currentDate = new Date();
        businessObject.setCreatedDate(currentDate);
        businessObject.setModifiedDate(currentDate);
        return crudServiceDAO.create(businessObject, businessObjectConfig);

    }

    protected BusinessObject update(BusinessObject businessObject) {

        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigByName(
                loader.getConfiguration(), businessObject.getTypeName());

        return crudServiceDAO.update(businessObject, businessObjectConfig);

    }

    @Override
    public BusinessObject save(BusinessObject businessObject) {

        if (businessObject.isNew()) {
            return create(businessObject);
        }

        return update(businessObject);

    }

    @Override
    public List<BusinessObject> save(List<BusinessObject> businessObjects) {
        List<BusinessObject> result = new ArrayList();

        for (BusinessObject  businessObject : businessObjects) {
            BusinessObject newBusinessObject;
            try {
                newBusinessObject = save(businessObject);
                result.add(newBusinessObject);
            } catch (Exception e) {
                // TODO: пока ничего не делаем...разобраться как обрабатывать ошибки
            }

        }

        return result;

    }

    @Override
    public boolean exists(Id id) {
        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigById(
                loader.getConfiguration(), id);

        return crudServiceDAO.exists(id, businessObjectConfig);
    }

    @Override
    public BusinessObject find(Id id) {
        return crudServiceDAO.find(id);
    }

    @Override
    public List<BusinessObject> find(List<Id> ids) {
        return crudServiceDAO.find(ids);
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filterValues, SortOrder sortOrder, int offset, int limit) {

        CollectionConfiguration collectionsConfiguration = loader.getCollectionConfiguration();

        if (collectionsConfiguration == null) {
            new RuntimeException("Collection configuration is not loaded");
        }

        CollectionConfig collectionConfig = collectionsConfiguration.findCollectionConfigByName(collectionName);

        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        return crudServiceDAO.findCollection(collectionConfig, filledFilterConfigs, sortOrder, offset, limit);
    }

    /**
     * Заполняет конфигурации фильтров значениями. Возвращает заполненные конфигурации фильтров (для которых были переданы значения). Сделан публичным для
     * тестов.
     * @param filterValues
     * @param collectionConfig
     * @return
     */
    public List<CollectionFilterConfig> findFilledFilterConfigs(List<Filter> filterValues, CollectionConfig collectionConfig) {
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
    public int findCollectionCount(String collectionName, List<Filter> filterValues) {

        CollectionConfiguration collectionsConfiguration = loader.getCollectionConfiguration();

        if (collectionsConfiguration == null) {
            new RuntimeException("Collection configuration is not loaded");
        }

        CollectionConfig collectionConfig = collectionsConfiguration.findCollectionConfigByName(collectionName);

        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        return crudServiceDAO.findCollectionCount(collectionConfig, filledFilterConfigs);
    }

    @Override
    public void delete(Id id) {


        RdbmsId rdbmsId = (RdbmsId)id;

        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigByName(
                loader.getConfiguration(), rdbmsId.getTypeName());

        crudServiceDAO.delete(id, businessObjectConfig);

    }

    @Override
    public int delete(Collection<Id> ids) {
        // TODO как обрабатывать ошибки при удалении каждого бизнесс объекта...
        int count = 0;
        for(Id id : ids) {
            try {
                delete(id);

                count++;
            } catch (ObjectNotFoundException e) {
                //ничего не делаем пока
            } catch (InvalidIdException e) {
                ////ничего не делаем пока
            }

        }
        return count;
    }

    @Override
    public int deleteAll(String businessObjectName) {

        BusinessObjectConfig businessObjectConfig = ConfigurationHelper.findBusinessObjectConfigByName(
                loader.getConfiguration(), businessObjectName);

        return crudServiceDAO.deleteAll(businessObjectConfig);
    }

}
