package ru.intertrust.cm.core.business.impl.access;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.ExecuteActionAccessType;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.AccessException;

@Stateless
@Local(AccessVerificationService.class)
@Remote(AccessVerificationService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class AccessVerificationServiceImpl implements AccessVerificationService {

    final static Logger logger = LoggerFactory.getLogger(AccessVerificationServiceImpl.class);
    
    @Autowired
    private AccessControlService accessControlService;
    
    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Override
    public boolean isReadPermitted(Id objectId) {

        String currentUser = currentUserAccessor.getCurrentUser();

        AccessToken accessToken = accessControlService.createAccessToken(currentUser, objectId, DomainObjectAccessType.READ);

        try {
            accessControlService.verifyDeferredAccessToken(accessToken, objectId, DomainObjectAccessType.READ);
        } catch (AccessException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Read permission to " + objectId + " is denied for user:" + currentUser);
            }
            return false;

        }
        return true;
    }

    @Override
    public boolean isWritePermitted(Id objectId) {
        String currentUser = currentUserAccessor.getCurrentUser();
        boolean result = accessControlService.verifyAccess(currentUser, objectId, DomainObjectAccessType.WRITE);
        if (!result){
            if (logger.isDebugEnabled()) {
                logger.debug("Write permission to " + objectId + " is denied for user:" + currentUser);
            }
        }
        return result;
    }

    @Override
    public boolean isDeletePermitted(Id objectId) {
        String currentUser = currentUserAccessor.getCurrentUser();
        boolean result = accessControlService.verifyAccess(currentUser, objectId, DomainObjectAccessType.DELETE);
        if (!result) {
            if (logger.isDebugEnabled()) {
                logger.debug("Delete permission to " + objectId + " is denied for user:" + currentUser);
            }
        }
        return result;
    }

    @Override
    public boolean isCreatePermitted(String domainObjectType) {

        String currentUser = currentUserAccessor.getCurrentUser();
        try {
            accessControlService.createDomainObjectCreateToken(currentUser, domainObjectType, null);
        } catch (AccessException ex) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isCreatePermitted(DomainObject domainObject) {

        String currentUser = currentUserAccessor.getCurrentUser();
        try {
            accessControlService.createDomainObjectCreateToken(currentUser, domainObject);
        } catch (AccessException ex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCreateChildPermitted(String domainObjectType, Id parentObjectId) {

        String currentUser = currentUserAccessor.getCurrentUser();
        try {
            accessControlService.createDomainObjectCreateToken(currentUser, domainObjectType,
                    new Id[] {parentObjectId });
        } catch (AccessException ex) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isExecuteActionPermitted(String actionName, Id objectId) {
        AccessType accessType = new ExecuteActionAccessType(actionName);
        String currentUser = currentUserAccessor.getCurrentUser();

        try {
            accessControlService.createAccessToken(currentUser, objectId, accessType);
        } catch (AccessException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Execute action " + actionName + " on object " + objectId + " is denied for user:" + currentUser);
            }
            return false;
        }

        return true;
    }
}
