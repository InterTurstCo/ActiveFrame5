package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;

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
    @Autowired
    private PermissionServiceDao permissionServiceDao;
    
    @Resource
    private SessionContext context;    
    
    @Autowired
    private PersonServiceDao personServiceDao;
    
    
    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId) {
        String personLogin = context.getCallerPrincipal().getName();
        Id personId = personServiceDao.findPersonByLogin(personLogin).getId();
        
        return permissionServiceDao.getObjectPermission(domainObjectId, personId);
    }

    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId, Id userId) {
        return permissionServiceDao.getObjectPermission(domainObjectId, userId);
    }

    @Override
    public List<DomainObjectPermission> getObjectPermissions(Id domainObjectId) {
        return permissionServiceDao.getObjectPermissions(domainObjectId);
    }

}
