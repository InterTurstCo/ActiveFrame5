package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.dao.api.ClassPathScanService;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.model.FatalException;

import java.util.HashMap;
import java.util.Map;

@ComponentName("ScheduledTaskEnumerator")
public class TaskListProvider implements EnumerationMapProvider {

    @Override
    @SuppressWarnings("rawtypes")
    public Map<String, Value> getMap(WidgetContext widgetContext) {
        return getTaskList();
    }

    @SuppressWarnings("rawtypes")
    private static HashMap<String, Value> taskList;

    @Autowired
    private ClassPathScanService scanner;

    @SuppressWarnings("rawtypes")
    private synchronized Map<String, Value> getTaskList() {
        if (taskList == null) {
            taskList = new HashMap<>();
            for (BeanDefinition bd : scanner.findClassesByAnnotation(ScheduleTask.class)) {
                String className = bd.getBeanClassName();
                StringValue value = new StringValue(className);
                if (taskList.containsValue(value)) {
                    continue;
                }
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    // Must not happen because we've searched classes already loaded
                    throw new FatalException("Error loading class " + className, e);
                }
                ScheduleTask taskAnnotation = clazz.getAnnotation(ScheduleTask.class);
                String taskName = taskAnnotation.name();
                if (taskName.isEmpty()) {
                    taskName = clazz.getSimpleName();
                }
                taskList.put(taskName, value);
            }
        }
        return taskList;
    }
}
