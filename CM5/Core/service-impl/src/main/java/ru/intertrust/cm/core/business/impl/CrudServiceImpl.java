package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Реализация сервиса для работы c базовы CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 *
 */
@Stateless
@Local(CrudService.class)
@Remote(CrudService.Remote.class)
public class CrudServiceImpl implements CrudService, CrudService.Remote {

    @Autowired
    private DomainObjectDao domainObjectDao;

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
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

    @Override
    public DomainObject save(DomainObject domainObject) {
        return domainObjectDao.save(domainObject);
    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {
        return domainObjectDao.save(domainObjects);
    }

    @Override
    public boolean exists(Id id) {
        return domainObjectDao.exists(id);
    }

    @Override
    public DomainObject find(Id id) {
        return domainObjectDao.find(id);
    }

    @Override
    public List<DomainObject> find(List<Id> ids) {
        return domainObjectDao.find(ids);
    }

    @Override
    public void delete(Id id) {
        domainObjectDao.delete(id);
    }

    @Override
    public int delete(Collection<Id> ids) {
        return domainObjectDao.delete(ids);
    }
}
