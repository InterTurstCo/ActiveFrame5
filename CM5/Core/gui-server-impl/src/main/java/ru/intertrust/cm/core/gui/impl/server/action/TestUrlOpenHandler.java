package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

/**
 * Created by Ravil on 21.09.2017.
 */
@ComponentName("open-test-url")
public class TestUrlOpenHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {
    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        Id taskId = context.getRootObjectId();
        SimpleActionData sData = new SimpleActionData();
        sData.setUrlToOpen("http://www.mail.ru");
        return sData;
    }
}
