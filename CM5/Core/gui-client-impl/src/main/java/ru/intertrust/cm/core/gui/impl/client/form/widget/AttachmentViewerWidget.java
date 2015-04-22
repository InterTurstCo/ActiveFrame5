package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.Iterator;

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
        Iterator<Widget> iterator = panel.iterator();
        Frame frame = (Frame)iterator.next();
        if(widgetState.getUrl()!=null)
               frame.setUrl(widgetState.getUrl());
        frame.setWidth(widgetState.getCurrentWidth());
        frame.setHeight(widgetState.getCurrentHeight());
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected WidgetState createNewState() {
        return null;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        Panel panel = new HorizontalPanel();
        Frame frame = new Frame();
        panel.add(frame);
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
