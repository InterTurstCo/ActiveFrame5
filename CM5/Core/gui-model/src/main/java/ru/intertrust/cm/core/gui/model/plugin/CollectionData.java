package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.Collection;
import java.util.HashMap;


/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionData implements Dto {

    private  HashMap<String, Object> value;

    public CollectionData(HashMap<String, Object> value) {
        this.value=value;
    }

    public CollectionData() {
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
