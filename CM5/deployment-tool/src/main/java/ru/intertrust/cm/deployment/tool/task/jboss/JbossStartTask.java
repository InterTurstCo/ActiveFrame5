package ru.intertrust.cm.deployment.tool.task.jboss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.deployment.tool.service.JbossService;
import ru.intertrust.cm.deployment.tool.task.Task;
import ru.intertrust.cm.deployment.tool.task.TaskContext;
import ru.intertrust.cm.deployment.tool.task.TaskResult;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
@Component
@Scope("prototype")
public class JbossStartTask implements Task {

    protected static Logger logger = LoggerFactory.getLogger(JbossStartTask.class);

    private TaskContext context;

    @Autowired
    private JbossService jbossService;

    @Override
    public TaskResult call() throws Exception {
        if (!jbossService.isServerStarted()) {
            jbossService.start();
        }
        return new TaskResult(true);
    }

    @Override
    public TaskContext getContext() {
        return context;
    }

    @Override
    public void setContext(TaskContext context) {
        this.context = context;
    }
}
