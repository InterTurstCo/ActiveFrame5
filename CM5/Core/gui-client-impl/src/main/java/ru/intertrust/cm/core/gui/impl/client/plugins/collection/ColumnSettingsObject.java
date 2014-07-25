package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Sergey.Okolot
 *         Created on 24.07.2014 11:47.
 */
public class ColumnSettingsObject extends JavaScriptObject {

    protected ColumnSettingsObject() {
    }

    public static native ColumnSettingsObject createObject() /*-{
        return {width: 0};
    }-*/;

    public final native int getWidth() /*-{
        return this.width;
    }-*/;

    public final native void setWidth(final int width) /*-{
        this.width = width;
    }-*/;
}