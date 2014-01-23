package ru.intertrust.cm.core.dao.impl.personmanager;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

import java.util.List;

/**
 * Точка расширения после сохранения или удаления пользователей.
 * Инвалидирует кеш персон.
 */
@ExtensionPoint(filter = "Person")
public class OnChangePersonExtensionPoint implements AfterSaveExtensionHandler, BeforeDeleteExtensionHandler {

    @Autowired
    private PersonServiceDao personServiceDao;


    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        evictCache(domainObject);
    }

    @Override
    public void onBeforeDelete(DomainObject deletedDomainObject) {
        evictCache(deletedDomainObject);
    }

    private void evictCache(DomainObject person){
        String login = person.getString("Login");
        if (login != null) {
            personServiceDao.personUpdated(login);
        }
    }
}
