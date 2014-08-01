package ru.intertrust.cm.core.gui.impl.client.history;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 13:12.
 */
public class HistoryItemObject extends JavaScriptObject {

    protected HistoryItemObject() {
    }

    public static native HistoryItemObject createObject(final String type, final String value) /*-{
        return {type: type, value: value};
    }-*/;

    public final native String getType() /*-{
        return this.type;
    }-*/;

    public final native String getValue() /*-{
        return this.value;
    }-*/;
}
