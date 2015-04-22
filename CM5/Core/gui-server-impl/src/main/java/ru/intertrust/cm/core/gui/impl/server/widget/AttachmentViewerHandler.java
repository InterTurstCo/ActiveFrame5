package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.AttachmentViewerConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.04.2015
 */
@ComponentName("attachment-viewer")
public class AttachmentViewerHandler extends WidgetHandler {
    @Override
    public AttachmentViewerState getInitialState(WidgetContext context) {
        FieldPath[] fieldPaths = context.getFieldPaths();
        AttachmentViewerConfig config = context.getWidgetConfig();
        AttachmentViewerState state = new AttachmentViewerState();
        state.setCurrentWidth((config.getWidth()==null)?config.getMaxTooltipWidth():config.getWidth());
        state.setCurrentHeight((config.getHeight()==null)?config.getMaxTooltipHeight():config.getHeight());
        state.setUrl("http://www.hse.gov.uk/pubns/indg171.pdf");
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }
}
