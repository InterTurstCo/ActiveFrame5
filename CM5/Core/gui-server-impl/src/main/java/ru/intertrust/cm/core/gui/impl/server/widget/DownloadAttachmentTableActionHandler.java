package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionResponse;

/**
 * @author Lesia Puhova
 *         Date: 24.12.2014
 *         Time: 18:55
 */
@ComponentName("download.attachment.table.action")
public class DownloadAttachmentTableActionHandler extends LinkedTableActionHandler {

    @Autowired
    private AttachmentService attachmentService;

    @Override
    public LinkedTableActionResponse handle(Dto request) {

        return new LinkedTableActionResponse();
    }

}
