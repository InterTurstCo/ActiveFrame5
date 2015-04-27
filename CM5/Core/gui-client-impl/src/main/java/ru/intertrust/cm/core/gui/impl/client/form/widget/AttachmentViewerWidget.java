package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;



/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.04.2015
 */
@ComponentName("attachment-viewer")
public class AttachmentViewerWidget extends BaseWidget {
    private AttachmentViewerState widgetState;


    @Override
    public void setCurrentState(WidgetState currentState) {
        widgetState = (AttachmentViewerState) currentState;
        Panel panel = (Panel) impl;
        if (widgetState.getUrl() != null)
            panel.add(new HTML("<embed src='" + com.google.gwt.core.client.GWT.getHostPageBaseURL()
                    + widgetState.getUrl() + "' width='" + widgetState.getCurrentWidth()
                    + "' height='" + widgetState.getCurrentHeight() + "'></embed>"));
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected WidgetState createNewState() {
        return new AttachmentViewerState();
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        Panel panel = new HorizontalPanel();
        panel.setStyleName("gwt-attachment-viewer-panel");
        return panel;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    @Override
    public Component createNew() {
        return new AttachmentViewerWidget();
    }


}
