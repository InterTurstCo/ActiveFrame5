package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.impl.server.action.access.AccessChecker;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionRequest;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionResponse;

/**
 * Created by andrey on 21.12.14.
 */
public abstract class LinkedTableActionHandler implements ComponentHandler {

    @Autowired
    ApplicationContext applicationContext;

    public abstract LinkedTableActionResponse handle(Dto request);

    public LinkedTableActionResponse execute(Dto request) {
        // do checks
        LinkedTableActionRequest linkedTableActionRequest = (LinkedTableActionRequest) request;
        LinkedTableActionResponse response = new LinkedTableActionResponse();
        String checkingComponent = linkedTableActionRequest.getObjectId() == null ?
                linkedTableActionRequest.getNewObjectsAccessCheckerComponent() :
                linkedTableActionRequest.getAccessCheckerComponent();
        if (checkingComponent != null) {
            AccessChecker accessChecker = (AccessChecker) applicationContext.getBean(checkingComponent);
            if (accessChecker != null) {
                if (accessChecker.checkAccess(linkedTableActionRequest.getObjectId())) {
                    return handle(request);
                } else {
                    response.setAccessGranted(false);
                }
            }
        }
        return response;
    }
}
