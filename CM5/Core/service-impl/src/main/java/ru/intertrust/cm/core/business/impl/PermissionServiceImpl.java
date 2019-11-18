package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

/**
 * Сервис получения прав пользователя на доменные объекты
 * @author larin
 *
 */
@Stateless(name = "PermissionService")
@Local(PermissionService.class)
@Remote(PermissionService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private PermissionServiceDao permissionServiceDao;
    
    @Autowired    
    private ConfigurationExplorer configurationExplorer;
    
    @Autowired
    private PersonServiceDao personServiceDao;

    @Autowired
    private DynamicGroupService dynamicGroupService;
    
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    
    
    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId) {
        try {
            String personLogin = currentUserAccessor.getCurrentUser();
            Id personId = personServiceDao.findPersonByLogin(personLogin).getId();

            return permissionServiceDao.getObjectPermission(domainObjectId, personId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId, Id userId) {
        try {
            return permissionServiceDao.getObjectPermission(domainObjectId, userId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObjectPermission> getObjectPermissions(Id domainObjectId) {
        try {
            return permissionServiceDao.getObjectPermissions(domainObjectId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public boolean isReadPermittedToEverybody(String domainObjectType) {
        try {
            return configurationExplorer.isReadPermittedToEverybody(domainObjectType);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void refreshAcls() {
        try {
            permissionServiceDao.refreshAcls();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void refreshAclFor(Id domainObjectId) {
        try {
            permissionServiceDao.refreshAclFor(domainObjectId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void recalcGroup(Id groupId) {
        try {
            dynamicGroupService.recalcGroup(groupId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }    
}
