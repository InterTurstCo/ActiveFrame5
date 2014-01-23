package ru.intertrust.cm.test.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.extension.AfterCreateExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter="country")
public class CreateCountryExtensionPoint implements AfterCreateExtentionHandler{

    @Override
    public void onAfterCreate(DomainObject createdDomainObject) {
        createdDomainObject.setString("name", "Необходимо ввести наименование страны!!!");        
    }

}
