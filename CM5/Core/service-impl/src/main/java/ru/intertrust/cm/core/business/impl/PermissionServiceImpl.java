package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.model.UnexpectedException;

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
    
    @Resource
    private SessionContext context;    
    
    @Autowired
    private PersonServiceDao personServiceDao;
    
    
    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId) {
        try {
            String personLogin = context.getCallerPrincipal().getName();
            Id personId = personServiceDao.findPersonByLogin(personLogin).getId();

            return permissionServiceDao.getObjectPermission(domainObjectId, personId);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PermissionService", "getObjectPermission",
                    "domainObjectId:" + domainObjectId, ex);
        }
    }

    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId, Id userId) {
        try {
            return permissionServiceDao.getObjectPermission(domainObjectId, userId);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PermissionService", "getObjectPermission",
                    "domainObjectId:" + domainObjectId + " userId:" + userId, ex);
        }
    }

    @Override
    public List<DomainObjectPermission> getObjectPermissions(Id domainObjectId) {
        try {
            return permissionServiceDao.getObjectPermissions(domainObjectId);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PermissionService", "getObjectPermissions",
                    "domainObjectId:" + domainObjectId, ex);
        }
    }

    @Override
    public boolean isReadPermittedToEverybody(String domainObjectType) {
        try {
            return configurationExplorer.isReadPermittedToEverybody(domainObjectType);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PermissionService", "isReadPermittedToEverybody",
                    "domainObjectType:" + domainObjectType, ex);
        }
    }
    
}
