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
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.CrudException;
import ru.intertrust.cm.core.model.SystemException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import javax.interceptor.Interceptors;
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

    @Autowired    
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    
    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

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
        checkForAttachment(domainObject.getTypeName());
        AccessToken accessToken = null;
        if (!domainObject.isNew()) {
            String user = currentUserAccessor.getCurrentUser();
            Id objectId = ((GenericDomainObject) domainObject).getId();
            accessToken = accessControlService.createAccessToken(user, objectId, DomainObjectAccessType.WRITE);
        } else {
            accessToken = createSystemAccessToken();              
        }
        return domainObjectDao.save(domainObject, accessToken);
    }


    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {

        for (DomainObject domainObject : domainObjects) {
           checkForAttachment(domainObject.getTypeName());
        }

        AccessToken accessToken = createSystemAccessToken();

        return domainObjectDao.save(domainObjects, accessToken);
    }

    private AccessToken createSystemAccessToken() {
        return accessControlService.createSystemAccessToken("CrudService");
    }

    @Override
    public boolean exists(Id id) {
        return domainObjectDao.exists(id);
    }

    @Override
    public DomainObject find(Id id) {
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.READ);
        return domainObjectDao.find(id, accessToken);
    }

    @Override
    public DomainObject findAndLock(Id id) {
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.WRITE);
        return domainObjectDao.findAndLock(id, accessToken);
    }

    @Override
    public List<DomainObject> find(List<Id> ids) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("Ids list can not be empty");
        }
        Id[] idsArray = ids.toArray(new Id[ids.size()]);
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken =
                accessControlService.createAccessToken(user, idsArray, DomainObjectAccessType.READ, false);

        return domainObjectDao.find(ids, accessToken);
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType) {
        if (domainObjectType == null || domainObjectType.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain Object type can not be null or empty");
        }
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken =
                accessControlService.createAccessToken(user, null, DomainObjectAccessType.READ);

        return domainObjectDao.findAll(domainObjectType, accessToken);
    }

    @Override
    public void delete(Id id) {
        String objectName = domainObjectTypeIdCache.getName(id);
        checkForAttachment(objectName);
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = null;
        accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.DELETE);
        domainObjectDao.delete(id, accessToken);
    }

    @Override
    public int delete(Collection<Id> ids) {
        if (ids == null || ids.size() == 0) {
            return 0;
        }
        for (Id id : ids) {
            String objectName = domainObjectTypeIdCache.getName(id);
            checkForAttachment(objectName);
        }

        Id[] idsArray = ids.toArray(new Id[ids.size()]);
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createAccessToken(user, idsArray, DomainObjectAccessType.DELETE, false);
        return domainObjectDao.delete(ids, accessToken);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField) {
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return domainObjectDao.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, accessToken);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField) {
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return domainObjectDao.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, accessToken);
    }

    @Override
    public String getDomainObjectType(Id id) {
        return domainObjectTypeIdCache.getName(id);
    }

    private void checkForAttachment(String objectType) {
        if (configurationExplorer.isAttachmentType(objectType)){
            throw new CrudException("Working with Attachments allowed only through AttachmentService");
        }
    }

}
