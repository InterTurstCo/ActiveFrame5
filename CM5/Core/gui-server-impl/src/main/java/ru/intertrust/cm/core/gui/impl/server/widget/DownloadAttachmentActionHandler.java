package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionResponse;

/**
 * @author Lesia Puhova
 *         Date: 24.12.2014
 *         Time: 18:55
 */
@ComponentName("download.attachment.action")
public class DownloadAttachmentActionHandler extends LinkedTableActionHandler {
    @Override
    public LinkedTableActionResponse handle(Dto request) {

        return new LinkedTableActionResponse();
    }
}
