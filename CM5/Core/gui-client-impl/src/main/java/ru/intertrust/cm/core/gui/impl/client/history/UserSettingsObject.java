package ru.intertrust.cm.core.gui.impl.client.history;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Sergey.Okolot
 *         Created on 24.07.2014 13:43.
 */
public class UserSettingsObject extends JavaScriptObject {

    protected UserSettingsObject() {
    }

    public final native JavaScriptObject getAttr(String key) /*-{
        return this[key];
    }-*/;

    public final native void setAttr(String key, JavaScriptObject object) /*-{
        this[key] = object;
    }-*/;
}

