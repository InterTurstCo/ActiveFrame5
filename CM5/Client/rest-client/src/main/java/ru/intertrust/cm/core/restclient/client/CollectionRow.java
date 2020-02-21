package ru.intertrust.cm.core.restclient.client;

import ru.intertrust.cm.core.restclient.model.CollectionRowData;
import ru.intertrust.cm.core.restclient.model.FieldData;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class CollectionRow extends DataBase{
    protected Map<String, Object> fields = new HashMap();

    public CollectionRow(CollectionRowData collectionRowData) throws ParseException {
        if (collectionRowData.getFields() != null) {
            for (FieldData restField : collectionRowData.getFields()) {
                fields.put(restField.getName().toLowerCase(), getFieldValue(restField));
            }
        }
    }

    public Object getValue(String name){
        return fields.get(name.toLowerCase());
    }


}
