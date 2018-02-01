package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.AttachmentAvailabilityActionContext;
import ru.intertrust.cm.core.gui.model.action.DownloadAttachmentActionContext;
import ru.intertrust.cm.core.model.SystemException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.server.widget.AttachmentDownloader.ATTACHMENT_TEMP_STORAGE_PLACEHOLDER;

/**
 * @author Lesia Puhova
 *         Date: 24.12.2014
 *         Time: 18:55
 *         <p/>
 *         Пример реализации обработчика для получения id вложения по id доменного объекта.
 *         Если вложений больше одного, возвращаем первое попавшееся.
 *         Этот класс нужен в первую очередь для тестирования действия скачивания вложений. В реальном приложении, вероятно,
 *         оно будет заменено другим действием, логика которого соответствует конкретным требованиям.
 */
@ComponentName("download.attachment.action")
public class DownloadAttachmentActionHandler implements ComponentHandler {

    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private PropertyResolver propertyResolver;

    // used for async call from client
    public Dto getAttachmentId(Dto parentObjectId) {
        List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor((Id) parentObjectId);
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        return attachments.get(0).getId();
    }

    public Dto checkAvailability(Dto context) {
        DownloadAttachmentActionContext downloadAttachmentActionContext = (DownloadAttachmentActionContext) context;
        AttachmentAvailabilityActionContext availabilityActionContext = new AttachmentAvailabilityActionContext();
        availabilityActionContext.setAvailable(isAvailable(downloadAttachmentActionContext));
        return availabilityActionContext;
    }

    private boolean isAvailable(DownloadAttachmentActionContext context) {
        Id id = context.getId();
        String tempName = context.getTempName();
        boolean result;
        try {
            result = id == null ? availableFromTempDir(tempName) : availableFromSavedAttachment(id);
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private boolean availableFromSavedAttachment(Id id) throws IOException {
        RemoteInputStream remoteFileData = createRemoteInputStream(id);
        try (InputStream inputStream = RemoteInputStreamClient.wrap(remoteFileData);) {
            return true;
        }

    }

    private boolean availableFromTempDir(String tempName) throws IOException {
        String attachmentTempStoragePath = propertyResolver.resolvePlaceholders(ATTACHMENT_TEMP_STORAGE_PLACEHOLDER);
        try (InputStream inputStream = new FileInputStream(attachmentTempStoragePath + tempName)) {
            return true;
        }

    }

    private RemoteInputStream createRemoteInputStream(Id id) {
        RemoteInputStream remoteFileData = null;
        try {
            remoteFileData = attachmentService.loadAttachment(id);
        } catch (SystemException ex) {

        }
        return remoteFileData;
    }
}
