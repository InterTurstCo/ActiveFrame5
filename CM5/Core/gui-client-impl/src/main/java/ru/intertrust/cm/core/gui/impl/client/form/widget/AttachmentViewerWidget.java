package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.ShowAttachmentEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ShowAttachmentEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;


/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.04.2015
 */
@ComponentName("attachment-viewer")
public class AttachmentViewerWidget extends BaseWidget implements ShowAttachmentEventHandler {
    private AttachmentViewerState widgetState;
    private Panel panel;
    private HandlerRegistration handlerRegistration;

    @Override
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        widgetState = (AttachmentViewerState) currentState;
        if (widgetState.isCommonUsage()) {
            clearHandlers();
            handlerRegistration = Application.getInstance().getEventBus()
                .addHandler(ShowAttachmentEvent.TYPE, this);
        }
        Panel panel = (Panel) impl;
        if (widgetState.getUrl() != null)
            panel.add(new HTML("<embed src='" + com.google.gwt.core.client.GWT.getHostPageBaseURL()
                    + widgetState.getUrl() + "' width='" + widgetState.getCurrentWidth()
                    + "' height='" + widgetState.getCurrentHeight() + "'></embed>"));
    }

    @Override
    public Object getValue() {
        return null;
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
        panel = new HorizontalPanel();
        panel.setStyleName("gwt-attachment-viewer-panel");
        onDetach(panel);
        return panel;
    }

    @Override
    protected void onDetach(Widget widget) {
        widget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                if (!attachEvent.isAttached()) {
                    clearHandlers();
                }
            }
        });
    }

    @Override
    protected void clearHandlers() {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    @Override
    public Component createNew() {
        return new AttachmentViewerWidget();
    }


    @Override
    public void showAttachmentEvent(ShowAttachmentEvent event) {
        if (event.getViewerId().equals(widgetState.getViewerId())) {
            panel.clear();
            panel.add(new HTML("<embed src='" + com.google.gwt.core.client.GWT.getHostPageBaseURL()
                    + widgetState.getDownloadServletName() + event.getObjectId().toStringRepresentation() + "' width='" + widgetState.getCurrentWidth()
                    + "' height='" + widgetState.getCurrentHeight() + "'></embed>"));
        }
    }
}
