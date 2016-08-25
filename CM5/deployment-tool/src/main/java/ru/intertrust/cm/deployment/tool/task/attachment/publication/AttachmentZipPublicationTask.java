package ru.intertrust.cm.deployment.tool.task.attachment.publication;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.deployment.tool.task.TaskResult;
import ru.intertrust.cm.deployment.tool.task.attachment.AttachmentZipTask;

import static ru.intertrust.cm.deployment.tool.property.AttachmentType.PUBLICATION_STORAGE;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
@Component
@Scope("prototype")
public class AttachmentZipPublicationTask extends AttachmentZipTask {

    @Override
    public TaskResult call() throws Exception {
        String path = serverProperties.get(PUBLICATION_STORAGE);
        return new TaskResult(zip(PUBLICATION_STORAGE, path));
    }
}
