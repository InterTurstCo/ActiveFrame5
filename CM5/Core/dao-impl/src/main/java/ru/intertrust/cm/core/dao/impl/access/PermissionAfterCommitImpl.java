package ru.intertrust.cm.core.dao.impl.access;

import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AclData;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.OnCalculateContextRoleExtensionHandler;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless
@Local(PermissionAfterCommit.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class PermissionAfterCommitImpl implements PermissionAfterCommit{
    @Autowired
    private ExtensionService extensionService;    

    @Override
    public void onAfterCommit(Map<Id, AclData> aclDatas) {
        OnCalculateContextRoleExtensionHandler handler =
                extensionService.getExtentionPoint(OnCalculateContextRoleExtensionHandler.class, null);

        for (Id id : aclDatas.keySet()) {
            handler.onCalculate(aclDatas.get(id), id);
        }
        
    }

}
