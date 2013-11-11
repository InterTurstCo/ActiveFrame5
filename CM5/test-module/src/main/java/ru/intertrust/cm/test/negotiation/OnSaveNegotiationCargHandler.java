package ru.intertrust.cm.test.negotiation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

/**
 * Точка расширения сохранения карточки согласования. Устанавливает атрибут
 * родительского согласования в идентификатор текущей карточки если он равен
 * null
 * @author larin
 * 
 */
@ExtensionPoint(filter = "Negotiation_Card")
public class OnSaveNegotiationCargHandler implements AfterSaveExtensionHandler {

    @Autowired
    private DomainObjectDao domainObjectDao; 

    @Autowired
    private AccessControlService accessControlService; 
    
    
    /**
     * Входня функция точки расширения. Проверяет родительское согласование и
     * устанавливает идетнификатор текущего согласования в случае если он равен
     * null
     */
    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        AccessToken token = accessControlService.createSystemAccessToken(this.getClass().getName());

        //Id parentNegotiation = domainObject.getReference("Add_Negotiation_Card");
        String name = domainObject.getString("Name");
        
        boolean objChanged = false;
        
        /*
        if (parentNegotiation == null) {
            domainObject.setReference("Add_Negotiation_Card", domainObject.getId());
            objChanged = true;
        }*/

        if (name == null || name.length() == 0) {
            DomainObject negotiator = domainObjectDao.find(domainObject.getReference("Negotiator"), token);
            domainObject.setString("Name", "Карточка согласующего " + negotiator.getString("Login"));
            objChanged = true;
        }        
        
        if (objChanged){
            domainObjectDao.save(domainObject, token);
        }

    }
}
