package ru.intertrust.cm.core.business.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.transaction.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import ru.intertrust.cm.core.business.api.FileSystemAttachmentCleaner;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.config.AttachmentStorageConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FolderStorageConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.model.ScheduleException;

/**
 * Периодическое задание удаляющее все файлы вложений в хранилище,
 * не имеющие ссылающихся на них доменных объектов.
 * Отключена в связи CMFIVE-22601
 */
/*@ScheduleTask(name = "FileSystemAttachmentCleanerScheduleTask", hour = "2", minute = "15", active = false,
              taskTransactionalManagement = true)*/
public class FileSystemAttachmentCleanerScheduleTask implements ScheduleTaskHandle {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemAttachmentCleanerScheduleTask.class);

    @Autowired
    private FileSystemAttachmentCleaner cleaner;

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) {
		// CMFIVE-22601 Отключаем задачу, if (true) нужно чтоб не падала компиляция
		if (true){
			return "Attention, task disabled. See CMFIVE-22601";
		}
		
        logger.info("FileSystemAttachmentCleanerScheduleTask started");
        String result = cleaner.clean(ejbContext);
        logger.info("FileSystemAttachmentCleanerScheduleTask finished");

        return result;
    }
}
