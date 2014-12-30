package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionRequest;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionResponse;

/**
 * Created by andrey on 21.12.14.
 */
@ComponentName("default.delete.table.action")
public class DefaultDeleteActionHandler extends LinkedTableActionHandler {
    @Override
    public LinkedTableActionResponse handle(Dto request) {
        LinkedTableActionRequest linkedTableActionRequest = (LinkedTableActionRequest) request;
        LinkedTableActionResponse response = new LinkedTableActionResponse();
        return response;
    }
}
