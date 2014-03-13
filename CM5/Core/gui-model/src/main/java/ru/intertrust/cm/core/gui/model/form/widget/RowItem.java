package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.HashMap;

public class RowItem implements Dto {

    HashMap<String, String> values = new HashMap<String, String>();
    HashMap<String, String> additionalParams = new HashMap<String, String>();
    private Id objectId;

    public String getValueByKey(String key) {
        return values.get(key);
    }

    public void setValueByKey(String key, String value) {
        values.put(key, value);
    }


    public void setObjectId(Id objectId) {
        this.objectId = objectId;
    }

    public Id getObjectId() {
        return objectId;
    }

    public void setParameter(String key, String value) {
        additionalParams.put(key, value);
    }

    public String getParameter(String key) {
        return additionalParams.get(key);
    }


}
