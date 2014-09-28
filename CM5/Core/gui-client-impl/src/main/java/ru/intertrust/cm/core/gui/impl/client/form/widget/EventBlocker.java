package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 28.09.2014
 *         Time: 21:59
 */
public class EventBlocker implements Event.NativePreviewHandler {
    private Widget blockedWidget;

    public EventBlocker(Widget blockedWidget) {
        this.blockedWidget = blockedWidget;
    }

    public void onPreviewNativeEvent(Event.NativePreviewEvent pEvent){
        Element target = pEvent.getNativeEvent().getEventTarget().cast();
        if (blockedWidget.getElement().isOrHasChild(target)){
            pEvent.getNativeEvent().stopPropagation();
            pEvent.getNativeEvent().preventDefault();
        }
    }
}
