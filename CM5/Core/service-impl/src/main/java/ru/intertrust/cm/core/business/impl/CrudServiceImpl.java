package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.xml.sax.SAXException;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import javax.interceptor.Interceptors;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Реализация сервиса для работы c базовы CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 */
@Stateless
@Local(CrudService.class)
@Remote(CrudService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class CrudServiceImpl implements CrudService, CrudService.Remote {

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @Override
    public IdentifiableObject createIdentifiableObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DomainObject createDomainObject(String name) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(name);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        return domainObject;
    }

    @Override
    public DomainObject save(DomainObject domainObject) {
        AccessToken accessToken = null;
        if (!domainObject.isNew()) {
            // TODO get userId from EJB Context
            String user = "admin";
            Id objectId = ((GenericDomainObject) domainObject).getId();
            accessToken = accessControlService.createAccessToken(user, objectId, DomainObjectAccessType.WRITE);
        } else {
            accessToken = accessControlService.createSystemAccessToken("CrudService");              
        }
        return domainObjectDao.save(domainObject, accessToken);
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
        // TODO get userId from EJB Context
        String user = "admin";
        AccessToken accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.READ);
        return domainObjectDao.find(id, accessToken);
    }

    @Override
    public List<DomainObject> find(List<Id> ids) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("Ids list can not be empty");
        }
        Id[] idsArray = ids.toArray(new Id[ids.size()]);
        // TODO get user from EJB Context
        String user= "admin";
        AccessToken accessToken =
                accessControlService.createAccessToken(user, idsArray, DomainObjectAccessType.READ, false);

        return domainObjectDao.find(ids, accessToken);
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType) {
        if (domainObjectType == null || domainObjectType.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain Object type can not be null or empty");
        }
        // TODO get user from EJB Context
        String user = "admin";
        AccessToken accessToken =
                accessControlService.createAccessToken(user, null, DomainObjectAccessType.READ);

        return domainObjectDao.findAll(domainObjectType, accessToken);
    }

    @Override
    public void delete(Id id) {
        //TODO get userId from EJB Context
        int userId = 1;
        AccessToken accessToken = null;
        //TODO закомментировал использование AcessToken до его работоспособности
        //accessToken = accessControlService.createAccessToken(userId, id, DomainObjectAccessType.DELETE);
        domainObjectDao.delete(id, accessToken);
    }

    @Override
    public int delete(Collection<Id> ids) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("Ids list can not be empty");
        }
        Id[] idsArray = ids.toArray(new Id[ids.size()]);
        // TODO get userId from EJB Context
        String user= "admin";
        accessControlService.createAccessToken(user, idsArray, DomainObjectAccessType.DELETE, false);
        return domainObjectDao.delete(ids);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField) {
        // TODO get userId from EJB Context
        String user = "admin";
        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return domainObjectDao.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, accessToken);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField) {
        // TODO get userId from EJB Context
        String user = "admin";

        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return domainObjectDao.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, accessToken);
    }
}
