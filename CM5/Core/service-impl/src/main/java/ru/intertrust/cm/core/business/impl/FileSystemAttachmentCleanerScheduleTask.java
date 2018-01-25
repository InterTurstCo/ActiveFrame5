package ru.intertrust.cm.core.business.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.model.ScheduleException;

/**
 * Периодическое задание удаляющее все файлы вложений в хранилище,
 * не имеющие ссылающихся на них доменных объектов.
 */
@ScheduleTask(name = "FileSystemAttachmentCleanerScheduleTask", hour = "2", minute = "15",
        active = true)
public class FileSystemAttachmentCleanerScheduleTask implements ScheduleTaskHandle {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemAttachmentCleanerScheduleTask.class);

    @org.springframework.beans.factory.annotation.Value("${attachment.storage}")
    private String attachmentSaveLocation;

    @org.springframework.beans.factory.annotation.Value("${attachment.delete.batch.size:100}")
    private int fileDeleteBatchSize;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private AttachmentContentDao attachmentContentDao;

    @Autowired
    private CollectionsService collectionsService;

    private String[] attachmentTypes;

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        logger.info("FileSystemAttachmentCleanerScheduleTask started");

        attachmentTypes = configurationExplorer.getAllAttachmentTypes();
        if (attachmentTypes == null || attachmentTypes.length == 0) return "COMPLETE";

        File storageDir = new File(attachmentSaveLocation);
        String storageDirAbsolutePath = storageDir.getAbsolutePath();
        List<File> allFiles = new ArrayList<>();
        readFiles(allFiles, storageDir, true);
        
        logger.info("Found {} files", allFiles.size());

        long allAttachments = allFiles.size();
        long processed = 0;
        long linked = 0;
        long deleted = 0;
        long error = 0;
        
        try {
            if (Status.STATUS_ACTIVE != ejbContext.getUserTransaction().getStatus()) {
                ejbContext.getUserTransaction().begin();
            }

            int counter = 0;

            for (File file : allFiles) {
                if (sessionContext.wasCancelCalled()){
                    logger.info("Task is cancaled by schedule subsystem");
                    break;
                }
                processed++;
                
                counter++;
                String absolutePath = file.getAbsolutePath();
                String relativePath = absolutePath.substring(storageDirAbsolutePath.length());
                String unixRelativePath = attachmentContentDao.toRelativeFromAbsPathFile(absolutePath);

                //TODO: Remove Windoes-related code when all clients move to unix-style paths of attachments
                boolean isLinked = isLinkedInDo(unixRelativePath) || (relativePath.startsWith("\\") && isLinkedInDo(relativePath));

                if (!isLinked) {
                    if (file.delete()) {
                        logger.info("File " + relativePath + " has not linked from Domain Objects and was deleted");
                        deleted++;
                    } else {
                        logger.error("File " + relativePath + " can not be deleted");
                        error++;
                    }
                }else{
                    linked++;
                }

                if (counter == fileDeleteBatchSize) {
                    counter = 0;
                    ejbContext.getUserTransaction().commit();
                    ejbContext.getUserTransaction().begin();
                }
            }
        } catch (NotSupportedException | SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
            try {
                ejbContext.getUserTransaction().rollback();
            } finally {
                throw new ScheduleException(e);
            }
        } finally {
            try {
                if (Status.STATUS_ACTIVE ==  ejbContext.getUserTransaction().getStatus()) {
                    ejbContext.getUserTransaction().commit();
                }
            } catch (SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
                throw new ScheduleException(e);
            }
        }

        String result = "All attachments: " + allAttachments + "; ";
        result += "Processed: " + processed + "; ";
        result += "Linked: " + linked + "; ";
        result += "Deleted: " + deleted + "; ";
        result += "Errors: " + error + "; ";
        
        logger.info(result);
        logger.info("FileSystemAttachmentCleanerScheduleTask finished.");
        return result;
    }

    private boolean isLinkedInDo(String relativePath) {

        List<Value> params = new ArrayList<>();
        Value relativePathValue = new StringValue(relativePath);
        params.add(relativePathValue);

        for (String attachmentType : attachmentTypes) {
            IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                    "select t.id from " + attachmentType + " t where t.path = {0}", params);
            if (collection != null && collection.size() > 0) {
                return true;
            }
        }

        return false;
    }

    public void readFiles(List<File> list, File dir, boolean root) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    readFiles(list, file, false);
                } else if (!root) {
                    list.add(file);
                }
            }
        }
    }


}
