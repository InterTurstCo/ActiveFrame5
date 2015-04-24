package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.AttachmentViewerConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.MultiObjectNode;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.04.2015
 */
@ComponentName("attachment-viewer")
public class AttachmentViewerHandler extends WidgetHandler {
    private static final String FIELD_MIMETYPE = "MimeType";
    private static final String MIME_PDF = "application/pdf";
    @Autowired
    CrudService crudService;



    @Override
    public AttachmentViewerState getInitialState(WidgetContext context) {
        AttachmentViewerConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        ObjectsNode node = context.getFormObjects().getNode(fieldPath);
        AttachmentViewerConfig config = context.getWidgetConfig();
        AttachmentViewerState state = new AttachmentViewerState();
        DomainObject dObject = null;
        if (node instanceof MultiObjectNode) {
            if (((MultiObjectNode) node).getDomainObjects() != null &&
                    ((MultiObjectNode) node).getDomainObjects().size() > 0) {
                dObject = ((MultiObjectNode) node).getDomainObjects().get(0);
            }
        } else if (node == null) {
            Id dObjectId = (Id) context.getFieldPlainValue();
            if (dObjectId != null) {
                dObject = crudService.find(dObjectId);
            }
        }

        if (dObject != null && dObject.getFields().contains(FIELD_MIMETYPE)
                && dObject.getString(FIELD_MIMETYPE).equals(MIME_PDF)) {
            state.setUrl(createServletUrl(dObject));
        }
        state.setCurrentWidth((config.getWidth() == null) ? config.getMaxTooltipWidth() : config.getWidth());
        state.setCurrentHeight((config.getHeight() == null) ? config.getMaxTooltipHeight() : config.getHeight());
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }

    private static String createServletUrl(DomainObject attachment) {
        StringBuilder url = new StringBuilder()
                .append("attachment-viewer-download?");
        if (attachment.getId() != null) {
            url.append("id=").append(attachment.getId().toStringRepresentation());
        }
        return url.toString();
    }
}
