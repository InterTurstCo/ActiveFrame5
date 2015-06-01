package ru.intertrust.cm.core.gui.impl.server.widget;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.model.FatalException;

@ComponentName("ScheduledTaskEnumerator")
public class TaskListProvider implements EnumerationMapProvider {

    @Override
    @SuppressWarnings("rawtypes")
    public Map<String, Value> getMap(WidgetContext widgetContext) {
        return getTaskList();
    }

    @SuppressWarnings("rawtypes")
    private static HashMap<String, Value> taskList;

    @Autowired private ModuleService moduleService;

    @SuppressWarnings("rawtypes")
    private synchronized Map<String, Value> getTaskList() {
        if (taskList == null) {
            taskList = new HashMap<>();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(ScheduleTask.class));
            for (String pkg : getAllPackages()) {
                for (BeanDefinition bd : scanner.findCandidateComponents(pkg)) {
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
        }
        return taskList;
    }

    private Collection<String> getAllPackages() {
        HashSet<String> packages = new HashSet<>();
        for (ModuleConfiguration modConf : moduleService.getModuleList()) {
            List<String> modPackages = modConf.getExtensionPointsPackages();
            if (modPackages != null) {
                for (String packageName : modPackages) {
                    packages.add(packageName);
                }
            }
        }
        return packages;
    }
}
