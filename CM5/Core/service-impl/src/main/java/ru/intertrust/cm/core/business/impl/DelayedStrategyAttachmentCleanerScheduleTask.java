package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;

/**
 * Периодическое задание удаляющее вложения из хранилища используещее стратегию delayed
 */
@ScheduleTask(name = "DelayedStrategyAttachmentCleanerScheduleTask",
        hour = "2", minute = "15",
        active = true,
        taskTransactionalManagement = true,
        timeout = 30)
public class DelayedStrategyAttachmentCleanerScheduleTask implements ScheduleTaskHandle {
    private static final Logger logger = LoggerFactory.getLogger(DelayedStrategyAttachmentCleanerScheduleTask.class);

    @Autowired
    private CrudService crudService;

    @Autowired
    private CollectionsService collectionsService;


    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) {
        long deleted = 0;
        long errors = 0;
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                "select id, path from delete_attachment_journal where delete_after < {0}",
                Collections.singletonList(new DateTimeValue(new Date())));

        for (IdentifiableObject row : collection) {
            if (sessionContext.wasCancelCalled()){
                break;
            }
            try {
                Path path = Paths.get(row.getString("path"));

                // Удаляем есть существует
                if (Files.exists(path)){
                    Files.delete(path);
                }

                // Проверяем удален ли файл
                if (!Files.exists(path)) {
                    crudService.delete(row.getId());
                    deleted++;
                }else{
                    errors++;
                }
            }catch(Exception ex){
                logger.error("Error delete attachnent file", ex);
                errors++;
            }
        }
        return "Deleted: " + deleted + ", Errors: " + errors;
    }
}
