package ru.intertrust.cm.core.business.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;

/**
 * Реализация сервиса для работы c базовыvb CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 *
 */
public class CrudServiceImpl implements CrudService {

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
        businessObject.setModifiedDate(new Date());

        return crudServiceDAO.create(businessObject, businessObjectConfig);

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
    public IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filters,
            SortOrder sortOrder, int offset, int limit) {
        // TODO Auto-generated method stub
        return null;
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
