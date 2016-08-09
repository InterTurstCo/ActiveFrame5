package ru.intertrust.cm.deployment.tool.task.ear;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.deployment.tool.property.UpgradeProperties;
import ru.intertrust.cm.deployment.tool.service.ResourceService;
import ru.intertrust.cm.deployment.tool.task.Task;
import ru.intertrust.cm.deployment.tool.task.TaskContext;
import ru.intertrust.cm.deployment.tool.task.TaskResult;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
@Component
@Scope("prototype")
public class EarCreateBackupTask implements Task {

    protected static Logger logger = LoggerFactory.getLogger(EarCreateBackupTask.class);

    private TaskContext context;

    @Autowired
    private UpgradeProperties props;

    @Autowired
    private ResourceService resourceService;

    @Override
    public TaskResult call() throws Exception {
        String ear = context.getEar();
        Path source = Paths.get(props.getJbossHome(), "standalone", "deployments");
        Path target = Paths.get(props.getBackupFolder(), ear);
        return new TaskResult(resourceService.copy(source, target, ear));
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
