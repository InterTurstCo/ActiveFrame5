package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionRequest;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionResponse;

/**
 * Created by andrey on 21.12.14.
 */
@Deprecated //looks like stub and not used for now
@ComponentName("default.edit.table.action")
public class DefaultEditActionHandler extends LinkedTableActionHandler {
    @Override
    public LinkedTableActionResponse handle(Dto request) {
        LinkedTableActionRequest linkedTableActionRequest = (LinkedTableActionRequest) request;
        System.out.println(linkedTableActionRequest.getAccessCheckerComponent());
        System.out.println(linkedTableActionRequest.getNewObjectsAccessCheckerComponent());
        LinkedTableActionResponse response = new LinkedTableActionResponse();
        return response;
    }
}
