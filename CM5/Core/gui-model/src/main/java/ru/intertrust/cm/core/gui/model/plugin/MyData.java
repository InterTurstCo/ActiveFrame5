package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.Collection;
import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 12.09.13
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
public class MyData implements Dto {

    private  HashMap<String, Object> value;

    public MyData(HashMap<String, Object> value) {
        this.value=value;
    }

    public <T> T getValueByKey(String key, Class<T> type) {
        return (T) this.value.get(key);
    }

    public void setValue(HashMap<String, Object> value) {
        this.value = value;
    }
    public Collection<Object> getRowValues() {
        return value.values();

    }

}
