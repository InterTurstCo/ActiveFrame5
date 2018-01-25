package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * Устанавливает статус из текстового поля ServerState. Необходим для тестирования прав в разных статусах. Пришлось делать
 * таким образом, из за невозможности установить статус через Remote интерфейс
 * @author larin
 * 
 */

@ExtensionPoint(filter = "Internal_Document")
public class OnAfterSaveInternalDocument implements AfterSaveExtensionHandler {

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CollectionsDao collectionsDao;

    private Id getStatus(String statusName, AccessToken accessToken) {
        IdentifiableObjectCollection collection =
                collectionsDao.findCollectionByQuery("select t.id from Status t where t.name = '" + statusName + "'",
                        0, 10, accessToken);
        Id result = null;
        if (collection.size() > 0) {
            result = collection.get(0).getId();
        }
        return result;
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        FieldModification fieldModification = getChangeState(changedFields);
        //Не обрабатываем новые объекты или в случае если статус не менялся        
        if (domainObject.getId() != null && fieldModification != null) {

            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

            //Получаем старое значение поля ServerState
            String oldStatus = fieldModification.getBaseValue() != null ? ((StringValue)fieldModification.getBaseValue()).get() : null;
            String newStatus = fieldModification.getComparedValue() != null ? ((StringValue)fieldModification.getComparedValue()).get() : null;

            //Если статус изменился то выполняем его установку через domainObjectDao
            if (newStatus != null && !newStatus.equals(oldStatus) || (newStatus == null && oldStatus != null)) {
                domainObjectDao.setStatus(domainObject.getId(), getStatus(newStatus, accessToken), accessToken);
            }
        }
    }

    private FieldModification getChangeState(List<FieldModification> changedFields) {
        if (changedFields != null) {
            for (FieldModification fieldModification : changedFields) {
                if (fieldModification.getName().equals("ServerState"))
                    return fieldModification;
            }
        }
        return null;
    }

}
