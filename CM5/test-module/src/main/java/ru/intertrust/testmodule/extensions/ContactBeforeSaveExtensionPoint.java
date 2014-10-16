package ru.intertrust.testmodule.extensions;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.extension.BeforeSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

import java.util.List;

/**
 * Created by Konstantin Gordeev on 13.10.2014.
 */
@ExtensionPoint(filter = "cont_contact_person")
public class ContactBeforeSaveExtensionPoint implements BeforeSaveExtensionHandler {

    @Override
    public void onBeforeSave(DomainObject domainObject, List<FieldModification> fieldModifications) {
        String name = domainObject.getString(Constants.FIELD_NAME_NAME);
        String surname = domainObject.getString(Constants.FIELD_NAME_SURNAME);
        String patronymic = domainObject.getString(Constants.FIELD_NAME_PATRONYMIC);
        StringBuilder sb = new StringBuilder();

        if(surname != null && !surname.trim().isEmpty()) {
            sb.append(surname).append(" ");
        }

        if(name != null && !name.trim().isEmpty()) {
            sb.append(name).append(" ");
        }

        if(patronymic != null && !patronymic.trim().isEmpty()) {
            sb.append(patronymic);
        }

        domainObject.setString(Constants.FIELD_NAME_TITLE, sb.toString());
    }
}
