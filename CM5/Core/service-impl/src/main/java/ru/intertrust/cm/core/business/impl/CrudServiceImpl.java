package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateExtentionHandler;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.core.model.CrudException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import java.util.ArrayList;
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

    @Autowired
    private ExtensionService extensionService;
    
    
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

        //Точка расширения после создания
        List<String> parentTypes = getAllParentTypes(name);
        for (String typeName : parentTypes) {
            AfterCreateExtentionHandler extension = extensionService.getExtentionPoint(AfterCreateExtentionHandler.class, typeName);
            extension.onAfterCreate(domainObject);
        }
        
        return domainObject;
    }

    /**
     * Получение всей цепочки родительских типов начиная от переданноготв параметре
     * @param name
     * @return
     */
    private List<String> getAllParentTypes(String name) {
        List<String> result = new ArrayList<String>();
        result.add(name);

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class, name);
        if (domainObjectTypeConfig.getExtendsAttribute() != null) {
            result.addAll(getAllParentTypes(domainObjectTypeConfig.getExtendsAttribute()));
        }

        return result;
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

        DomainObject result = domainObjectDao.save(domainObject, accessToken);
        if (result == null) {
            throw new ObjectNotFoundException(domainObject.getId());
        }

        return result;
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

        DomainObject result = domainObjectDao.find(id, accessToken);
        if (result == null) {
            throw new ObjectNotFoundException(id);
        }

        return result;
    }

    @Override
    public DomainObject findAndLock(Id id) {
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createAccessToken(user, id, DomainObjectAccessType.WRITE);

        DomainObject result = domainObjectDao.findAndLock(id, accessToken);
        if (result == null) {
            throw new ObjectNotFoundException(id);
        }

        return result;
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
