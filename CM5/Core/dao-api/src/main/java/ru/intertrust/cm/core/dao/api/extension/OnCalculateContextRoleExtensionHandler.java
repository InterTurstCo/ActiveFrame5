package ru.intertrust.cm.core.dao.api.extension;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AclData;

/**
 * Обработчик точки расширения, которая вызывается после пересчета состава acl каждой контекстной ролью
 * @author larin
 *
 */
public interface OnCalculateContextRoleExtensionHandler extends ExtensionPointHandler{

    void onCalculate(AclData aclData, Id domainObjectId); 
}
