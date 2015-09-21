package ru.intertrust.cm.core.business.impl;

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

import javax.ejb.SessionContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private CollectionsService collectionsService;

    private String[] attachmentTypes;

    @Override
    public String execute(SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {

        logger.info("FileSystemAttachmentCleanerScheduleTask started");

        attachmentTypes = configurationExplorer.getAllAttachmentTypes();
        if (attachmentTypes == null || attachmentTypes.length == 0) return "COMPLETE";

        File storageDir = new File(attachmentSaveLocation);
        String storageDirAbsolutePath = storageDir.getAbsolutePath();
        List<File> allFiles = new ArrayList<>();
        readFiles(allFiles, storageDir, true);
        for (File file : allFiles) {
            String absolutePath = file.getAbsolutePath();
            String relativePath = absolutePath.substring(storageDirAbsolutePath.length());

            if (!isLinkedInDo(file.getName())) {
                if (file.delete()) {
                    logger.info("File " + relativePath + " has not linked from Domain Objects and was deleted");
                } else {
                    logger.error("File " + relativePath + " can not be deleted");
                }
            }
        }

        logger.info("FileSystemAttachmentCleanerScheduleTask finished.");

        return "COMPLETE";
    }

    private boolean isLinkedInDo(String name) {

        List<Value> params = new ArrayList<>();
        Value value = new StringValue("%" + name);
        params.add(value);

        for (String attachmentType : attachmentTypes) {
            IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                    "select t.id from " + attachmentType + " t where t.path like {0}", params);
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
