package ru.intertrust.cm.deployment.tool.task.postgres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.deployment.tool.service.PostgresService;
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
public class PostgresRestoreBackupTask implements Task {

    protected static Logger logger = LoggerFactory.getLogger(PostgresRestoreBackupTask.class);

    private TaskContext context;

    @Autowired
    private PostgresService postgresService;

    @Override
    public TaskResult call() throws Exception {
        postgresService.restore(context.getEar());
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
