package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 24.12.2014
 *         Time: 18:55
 *
 *   Пример реализации обработчика для получения id вложения по id доменного объекта.
 *   Если вложений больше одного, возвращаем первое попавшееся.
 *   Этот класс нужен в первую очередь для тестирования действия скачивания вложений. В реальном приложении, вероятно,
 *   оно будет заменено другим действием, логика которого соответствует конкретным требованиям.
 */
@ComponentName("download.attachment.action")
public class DownloadAttachmentActionHandler implements ComponentHandler {

    @Autowired
    private AttachmentService attachmentService;

    // used for async call from client
    public Dto getAttachmentId(Dto parentObjectId) {
        List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor((Id)parentObjectId);
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        return attachments.get(0).getId();
    }
}
