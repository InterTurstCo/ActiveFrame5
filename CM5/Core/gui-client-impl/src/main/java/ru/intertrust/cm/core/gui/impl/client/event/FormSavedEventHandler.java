package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 25.12.2015
 */
public interface FormSavedEventHandler  extends EventHandler  {
    public void afterFormSaved(FormSavedEvent vent);
}
