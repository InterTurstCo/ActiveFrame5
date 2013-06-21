package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Реализация сервиса для работы c базовыvb CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 *
 */
@Stateless
@Local(CrudService.class)
@Remote(CrudService.Remote.class)
public class CrudServiceImpl implements CrudService, CrudService.Remote {

    public static final String QUERY_FILTER_PARAM_DELIMETER = ":";

    public static final String DEFAULT_CRITERIA_CONDITION = "and";

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    private CrudServiceDAO crudServiceDAO;

    public void setCrudServiceDAO(CrudServiceDAO crudServiceDAO) {
        this.crudServiceDAO = crudServiceDAO;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public IdentifiableObject createIdentifiableObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DomainObject createDomainObject(String name) {
        DomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(name);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        return domainObject;
    }

    protected DomainObject create(DomainObject domainObject) {

        DomainObjectConfig domainObjectConfig = configurationExplorer.getDomainObjectConfig(domainObject.getTypeName());
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return crudServiceDAO.create(domainObject, domainObjectConfig);

    }

    protected DomainObject update(DomainObject domainObject) {
        DomainObjectConfig domainObjectConfig = configurationExplorer.getDomainObjectConfig(domainObject.getTypeName());
        return crudServiceDAO.update(domainObject, domainObjectConfig);
    }

    @Override
    public DomainObject save(DomainObject domainObject) {

        if (domainObject.isNew()) {
            return create(domainObject);
        }

        return update(domainObject);

    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {
        List<DomainObject> result = new ArrayList();

        for (DomainObject domainObject : domainObjects) {
            DomainObject newDomainObject;
            try {
                newDomainObject = save(domainObject);
                result.add(newDomainObject);
            } catch (Exception e) {
                // TODO: пока ничего не делаем...разобраться как обрабатывать ошибки
            }

        }

        return result;

    }

    @Override
    public boolean exists(Id id) {
        return crudServiceDAO.exists(id);
    }

    @Override
    public DomainObject find(Id id) {
        return crudServiceDAO.find(id);
    }

    @Override
    public List<DomainObject> find(List<Id> ids) {
        return crudServiceDAO.find(ids);
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filterValues,
            SortOrder sortOrder, int offset, int limit) {

        CollectionConfig collectionConfig = configurationExplorer.getCollectionConfig(collectionName);
        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);
        return crudServiceDAO.findCollection(collectionConfig, filledFilterConfigs, filterValues, sortOrder, offset,
                limit);
    }

    /**
     * Заполняет конфигурации фильтров значениями. Возвращает заполненные конфигурации фильтров (для которых были
     * переданы значения). Сделан публичным для тестов.
     * @param filterValues
     * @param collectionConfig
     * @return
     */
    public List<CollectionFilterConfig> findFilledFilterConfigs(List<Filter> filterValues,
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
        String parameterPrefix = ":" + filterValue.getFilter();
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
            clonedFilterCriteria.setCondition(srcFilterCriteria.getCondition());
            clonedFilterCriteria.setValue(srcFilterCriteria.getValue());
            clonedFilterConfig.setFilterCriteria(clonedFilterCriteria);
        }

        clonedFilterConfig.setName(filterConfig.getName());

        return clonedFilterConfig;
    }

    @Override
    public int findCollectionCount(String collectionName, List<Filter> filterValues) {
        CollectionConfig collectionConfig = configurationExplorer.getCollectionConfig(collectionName);
        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);
        return crudServiceDAO.findCollectionCount(collectionConfig, filledFilterConfigs, filterValues);
    }

    @Override
    public void delete(Id id) {
        RdbmsId rdbmsId = (RdbmsId)id;
        DomainObjectConfig domainObjectConfig = configurationExplorer.getDomainObjectConfig(rdbmsId.getTypeName());
        crudServiceDAO.delete(id, domainObjectConfig);
    }

    @Override
    public int delete(Collection<Id> ids) {
        // TODO как обрабатывать ошибки при удалении каждого доменного объекта...
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
}
