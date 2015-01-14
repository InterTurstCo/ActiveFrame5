package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.impl.server.action.access.AccessChecker;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.CheckAccessRequest;
import ru.intertrust.cm.core.gui.model.action.CheckAccessResponse;

/**
 * Created by andrey on 29.12.14.
 */
@ComponentName("checkAccessHandler")
public class CheckAccessHandler implements ComponentHandler {
    @Autowired
    ApplicationContext applicationContext;

    public Dto checkAccess(Dto checkRequest) {
        CheckAccessRequest accessRequest = (CheckAccessRequest) checkRequest;
        String accesCheckerName = accessRequest.getAccessCheckerName();
        CheckAccessResponse response = new CheckAccessResponse();
        AccessChecker accessChecker = (AccessChecker) applicationContext.getBean(accesCheckerName);
        if (accessChecker != null) {
            response.setAccessGranted(accessChecker.checkAccess(accessRequest.getObjectId()));
        }
        return response;
    }
}
