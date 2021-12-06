package ru.intertrust.cm.core.restclient.client;

import ru.intertrust.cm.core.restclient.model.DomainObjectData;
import ru.intertrust.cm.core.restclient.model.FieldData;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainObject extends DataBase{


    private DomainObjectData data;
    private Map<String, Object> fields = new HashMap();

    private DomainObject(DomainObjectData data) throws ParseException {
        this.data = data;

        if (data.getFields() != null) {
            for (FieldData restField : data.getFields()) {
                fields.put(restField.getName().toLowerCase(), getFieldValue(restField));
            }
        }
    }

    public static DomainObject get(DomainObjectData data) throws ParseException {
        DomainObject helper = new DomainObject(data);
        return helper;
    }

    public String getId(){
        return data.getId();
    }

    public String getType(){
        return data.getType();
    }

    public static DomainObject create(String type) throws ParseException {
        DomainObjectData data = new DomainObjectData();
        data.setType(type);
        DomainObject helper = new DomainObject(data);
        return helper;
    }

    public Object getValue(String name){
        return fields.get(name.toLowerCase());
    }

    public void setValue(String name, Object value){
        if (value == null){
            fields.remove(name.toLowerCase());
        }else {
            fields.put(name.toLowerCase(), value);
        }
    }

    public DomainObjectData toDomainObjectData(){
        List<FieldData> dataFields = new ArrayList<>();
        data.setFields(dataFields);
        for (String name : fields.keySet()) {
            FieldData fieldData = new FieldData();
            fieldData.setName(name);
            dataFields.add(fieldData);

            Object value = fields.get(name);
            if (value instanceof Boolean){
                fieldData.setType(FieldData.TypeEnum.BOOLEAN);
                fieldData.setValue(((Boolean)value).toString());
            }else if(value instanceof String){
                fieldData.setType(FieldData.TypeEnum.STRING);
                fieldData.setValue(value.toString());
            }else if(value instanceof Long){
                fieldData.setType(FieldData.TypeEnum.LONG);
                fieldData.setValue(((Long)value).toString());
            }else if(value instanceof Double){
                fieldData.setType(FieldData.TypeEnum.DECIMAL);
                fieldData.setValue(((Double)value).toString());
            }else if(value instanceof LocalDate){
                fieldData.setType(FieldData.TypeEnum.TIMELESSDATE);
                fieldData.setValue(((LocalDate)value).format(dateFormat));
            }else if(value instanceof LocalDateTime){
                fieldData.setType(FieldData.TypeEnum.DATETIME);
                fieldData.setValue(((LocalDateTime)value).format(dateTimeFormat));
            }else if(value instanceof ZonedDateTime){
                fieldData.setType(FieldData.TypeEnum.DATETIMEWITHTIMEZONE);
                fieldData.setValue(((ZonedDateTime)value).format(dateTimeWithTimeZoneFormat));
            }
        }

        return data;
    }
}
