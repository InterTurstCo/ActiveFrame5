package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

@ComponentName("schedule-task-execute")
public class ScheduleTaskExecuteAction extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired
    private ScheduleService scheduleService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        Id taskId = context.getRootObjectId();
        if (taskId == null) {
            throw new GuiException("Задача для запуска не выбрана");
        }
        scheduleService.run(taskId);
        return new SimpleActionData();
    }

}
