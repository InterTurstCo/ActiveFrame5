package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.widget.DomainObjectTypeExtractor;
import ru.intertrust.cm.core.gui.model.ComponentName;

import javax.ejb.EJB;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.10.2014
 *         Time: 21:24
 */
@ComponentName("domain-object-type-extractor")
public class DomainObjectTypeExtractorImpl implements DomainObjectTypeExtractor {
    @Autowired
    private CrudService crudService;

    @EJB
    private PermissionService permissionService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;


    @Override
    public StringValue getType(Dto input) {
        Id id = (Id) input;
        return new StringValue(crudService.getDomainObjectType(id));
    }

    @Override
    public DomainObjectTypeAndAccessValue getTypeAndAccess(Dto input) {
        Id id = (Id) input;
        DomainObjectTypeAndAccessValue result = new DomainObjectTypeAndAccessValue();
        result.setDomainObjectType(crudService.getDomainObjectType(id));
        DomainObjectPermission permission = permissionService.getObjectPermission(id, currentUserAccessor.getCurrentUserId());
        if (permission != null && permission.getPermission() != null) {
            for(DomainObjectPermission.Permission prs : permission.getPermission()){
                if(DomainObjectPermission.Permission.Delete.equals(prs)){
                    result.setHasDeletePermission(true);
                }
                if(DomainObjectPermission.Permission.Write.equals(prs)){
                    result.setHasWritePermission(true);
                }
                if(DomainObjectPermission.Permission.Read.equals(prs)){
                    result.setHasReadPermission(true);
                }

            }
        }


        return result;
    }

}
