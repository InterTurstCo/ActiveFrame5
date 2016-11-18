package ru.intertrust.cm.deployment.tool.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.intertrust.cm.deployment.tool.property.RestoreVersionType;
import ru.intertrust.cm.deployment.tool.property.UpgradeProperties;
import ru.intertrust.cm.deployment.tool.service.JbossService;
import ru.intertrust.cm.deployment.tool.service.ResourceService;
import ru.intertrust.cm.deployment.tool.task.Task;
import ru.intertrust.cm.deployment.tool.task.TaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static java.util.Collections.singletonList;
import static ru.intertrust.cm.deployment.tool.property.RestoreVersionType.CURRENT;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
@Service
public class EarDeployManager {

    private static Logger logger = LoggerFactory.getLogger(EarDeployManager.class);

    @Autowired
    private UpgradeProperties props;

    @Autowired
    private JbossService jbossService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ResourceService resourceService;


    public void deploy() {
        logger.info("Starting deploy");

        //backup current deployed ear
        String ear = props.getEarCurrent();
        if (jbossService.isServerStarted()) {
            jbossService.undeployApplication(ear,props.getJbossCliController());
        }
        createBackup(ear);
        resourceService.cleanServer();

        //deploy sequence ears
        deploySequence();

        logger.info("Done", ear);
        taskExecutor.destroy();
    }

    private void deploySequence() {
        List<String> earSequence = props.getEarSequence();
        ListIterator<String> iterator = earSequence.listIterator();

        while (iterator.hasNext()) {
            String ear = iterator.next();
            boolean backup = false;
            boolean last = false;
            if (iterator.hasNext()) {
                String nextEar = iterator.next();
                backup = props.getBackupBefore().contains(nextEar);
                iterator.previous();
            } else {
                last = true;
            }
            boolean iterate = !deploy(ear, backup, last);
            if (iterate) {
                return;
            }
        }
    }

    private boolean deploy(String ear, boolean backup, boolean last) {
        logger.info("Starting deploy {}", ear);

        cleanInitData(ear);
        copyData(ear);

        if(!jbossService.isServerStarted()) {
            startServer();
        }

        if (jbossService.isDeploySuccess(ear)) {
            if (backup) {
                createBackup(ear);
            }
            if (!last) {
                jbossService.undeployApplication(ear,props.getJbossCliController());
                resourceService.cleanServer();
            }
            return true;
        } else {
            //restore logic
            logger.info("Start restore logic");
            jbossService.undeployApplication(ear,props.getJbossCliController());
            resourceService.cleanServer();

            ear = getForRestore(ear);
            logger.info("Starting restore {}", ear);
            restoreBackup(ear);

            String part = jbossService.isDeploySuccess(ear) ? "has been" : "do not";
            logger.info("Ear version - {} {} restored", ear, part);
        }
        return false;
    }

    private void startServer() {
        taskExecutor.execute(null, singletonList(taskExecutor.getJbossStartTask()));
    }

    private void cleanInitData(String ear) {
        if (!StringUtils.isEmpty(props.getInitialDataFolder())) {
            if (taskExecutor.execute(ear, singletonList(taskExecutor.getInitDataDeleteTask()))) {
                logger.info("Clean init data directory has been completed");
            }
        }
    }

    private void copyData(String ear) {
        List<Task> tasks = new ArrayList<>(2);
        tasks.add(taskExecutor.getEarToServerCopyTask());
        if (!StringUtils.isEmpty(props.getInitialDataFolder())) {
            tasks.add(taskExecutor.getInitDataCopyTask());
        }
        if (taskExecutor.execute(ear, tasks)) logger.info("Copy data has been completed");
    }

    private void createBackup(String ear) {
        resourceService.createBackupFolder(ear);
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(taskExecutor.getAttachmentZipTasks());
        tasks.add(taskExecutor.getPostgresCreateBackupTask());
        tasks.add(taskExecutor.getEarCreateBackupTask());
        if (taskExecutor.execute(ear, tasks)) logger.info("Backups has been completed");
    }

    private void restoreBackup(String ear) {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(taskExecutor.getAttachmentUnzipTasks());
        tasks.add(taskExecutor.getPostgresRestoreBackupTask());
        tasks.add(taskExecutor.getEarRestoreBackupTask());
        if (taskExecutor.execute(ear, tasks)) logger.info("Backups has been restored");
    }

    private String getForRestore(String ear) {
        RestoreVersionType restoreVersionType = props.getRestoreVersionOnFailure();
        return CURRENT.equals(restoreVersionType) ? props.getEarCurrent() : resourceService.getLastSuccessful(ear);
    }
}
