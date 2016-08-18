package ru.intertrust.cm.deployment.tool.task.attachment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.deployment.tool.property.AttachmentType;
import ru.intertrust.cm.deployment.tool.property.UpgradeProperties;
import ru.intertrust.cm.deployment.tool.service.ResourceService;
import ru.intertrust.cm.deployment.tool.task.Task;
import ru.intertrust.cm.deployment.tool.task.TaskContext;
import ru.intertrust.cm.deployment.tool.zip.UnzipHelper;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.EnumMap;

import static ru.intertrust.cm.deployment.tool.config.AppConstants.ARCHIVE_NAME_PATTERN;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */

public abstract class AttachmentUnzipTask implements Task {

    protected static Logger logger = LoggerFactory.getLogger(AttachmentZipTask.class);

    protected TaskContext context;

    @Autowired
    protected UpgradeProperties props;

    @Autowired
    protected ResourceService resourceService;

    protected EnumMap<AttachmentType, String> serverProperties;

    @Override
    public TaskContext getContext() {
        return context;
    }

    @Override
    public void setContext(TaskContext context) {
        this.context = context;
    }

    protected boolean unzip(AttachmentType attachmentType, String target) {
        String ear = context.getEar();
        String source = props.getBackupFolder() + File.separator + ear;
        String zipName = String.format(ARCHIVE_NAME_PATTERN, ear, attachmentType.getShortName());
        return new UnzipHelper().unzip(source, target, zipName);
    }

    @PostConstruct
    private void postConstruct() {
        serverProperties = resourceService.parseServerProperties();
    }
}
