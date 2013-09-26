package ru.intertrust.cm.core.gui.impl.client.panel;

import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 12.09.13
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
public class MyData {

    private transient HashMap<String, Object> value = new HashMap<String, Object>();

    public String type;
    public String name;
    public String name2;

    public MyData(String type, HashMap<String, Object>value, String name, String name2) {
        this.type=type;
        this.name = name;
        this.name2 = name2;
        this.value=value;
    }

    public <T> T getValueByKey(String key, Class<T> type) {
        return (T) this.value.get(key);
    }

    public void setValue(HashMap<String, Object> value) {
        this.value = value;
    }
}
