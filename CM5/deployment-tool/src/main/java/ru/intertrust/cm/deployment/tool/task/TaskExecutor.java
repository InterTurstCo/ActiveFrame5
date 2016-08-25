package ru.intertrust.cm.deployment.tool.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
@Component
public class TaskExecutor {

    private static Logger logger = LoggerFactory.getLogger(TaskExecutor.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ThreadPoolTaskExecutor exec;

    public boolean execute(String ear, List<Task> tasks) {
        if (!CollectionUtils.isEmpty(tasks)) {
            int size = tasks.size();
            List<Future<TaskResult>> futures = new ArrayList<>(size);
            for (Task task : tasks) {
                task.setContext(new TaskContext(ear));
                Future<TaskResult> future = exec.submit(task);
                futures.add(future);
            }

            List<TaskResult> results = new ArrayList<>(size);
            for (Future<TaskResult> future : futures) {
                try {
                    TaskResult taskResult = future.get();
                    results.add(taskResult);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }

            return parseResult(results);
        }
        return false;
    }

    private boolean parseResult(List<TaskResult> results) {
        boolean result = false;
        if (!CollectionUtils.isEmpty(results)) {
            for (TaskResult taskResult : results) {
                result = taskResult.isSuccess();
            }
        }
        return result;
    }

    public Task getAttachmentZipStorageTask() {
        return (Task) context.getBean("attachmentZipStorageTask");
    }

    public Task getAttachmentZipTempTask() {
        return (Task) context.getBean("attachmentZipTempTask");
    }

    public Task getAttachmentZipPublicationTask() {
        return (Task) context.getBean("attachmentZipPublicationTask");
    }

    public List<Task> getAttachmentZipTasks() {
        return new ArrayList<Task>() {
            {
                add(getAttachmentZipStorageTask());
                add(getAttachmentZipTempTask());
                add(getAttachmentZipPublicationTask());
            }
        };
    }

    public Task getAttachmentUnzipStorageTask() {
        return (Task) context.getBean("attachmentUnzipStorageTask");
    }

    public Task getAttachmentUnzipTempTask() {
        return (Task) context.getBean("attachmentUnzipTempTask");
    }

    public Task getAttachmentUnzipPublicationTask() {
        return (Task) context.getBean("attachmentUnzipPublicationTask");
    }

    public List<Task> getAttachmentUnzipTasks() {
        return new ArrayList<Task>() {
            {
                add(getAttachmentUnzipStorageTask());
                add(getAttachmentUnzipTempTask());
                add(getAttachmentUnzipPublicationTask());
            }
        };
    }

    public Task getEarCreateBackupTask() {
        return (Task) context.getBean("earCreateBackupTask");
    }

    public Task getEarRestoreBackupTask() {
        return (Task) context.getBean("earRestoreBackupTask");
    }

    public Task getEarToServerCopyTask() {
        return (Task) context.getBean("earToServerCopyTask");
    }

    public Task getInitDataCopyTask() {
        return (Task) context.getBean("initDataCopyTask");
    }

    public Task getInitDataDeleteTask() {
        return (Task) context.getBean("initDataDeleteTask");
    }

    public Task getJbossStartTask() {
        return (Task) context.getBean("jbossStartTask");
    }

    public Task getJbossStopTask() {
        return (Task) context.getBean("jbossStopTask");
    }

    public Task getPostgresCreateBackupTask() {
        return (Task) context.getBean("postgresCreateBackupTask");
    }

    public Task getPostgresRestoreBackupTask() {
        return (Task) context.getBean("postgresRestoreBackupTask");
    }

    public void destroy() {
        exec.shutdown();
    }
}
