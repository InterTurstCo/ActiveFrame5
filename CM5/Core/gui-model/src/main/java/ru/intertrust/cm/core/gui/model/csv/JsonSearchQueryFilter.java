package ru.intertrust.cm.core.gui.model.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vitaliy Orlov on 23.08.2016.
 */
public class JsonSearchQueryFilter {
    private String type;
    private String name;
    private List<JsonSearchQueryFilterValue> values;

    private Map<String, Object> compresedValues = null;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JsonSearchQueryFilterValue> getValues() {
        return values;
    }

    public void setValues(List<JsonSearchQueryFilterValue> values) {
        this.values = values;
    }

    public Boolean getBooleanValue(String propertyName){
        return getValue(propertyName, Boolean.class);
    }

    public String getStringValue(String propertyName){
        return getValue(propertyName, String.class);
    }

    public Double getDoubleValue(String propertyName) {
       return  getValue(propertyName, Double.class);
    }

    public Long getLongValue(String propertyName) {
        return  getValue(propertyName, Long.class);
    }

    public List<String> getStringListValue(String propertyName){
        return  getListValue(propertyName, String.class);
    }

    public <T> List<T> getListValue(String propertyName, Class<T> typeOfResultItem){
        List<T> result = new ArrayList<>();
        Map<String, Object> compressedMap = compressListValueToMap();
        Object propertyValue = compressedMap.get(propertyName);
        if(propertyValue == null ){
            return null;
        }

        if(propertyValue instanceof List){
            for(Object pItem : (List)propertyValue){
                if(typeOfResultItem.isInstance(pItem)){
                    result.add((T)pItem);
                }
            }
        }else{
            if(typeOfResultItem.isInstance(propertyValue)){
                result.add((T)propertyValue);
            }
        }

        return result;
    }

    public <T> T getValue(String propertyName, Class<T> targetResult){
        Map<String, Object> compressedMap = compressListValueToMap();
        Object result = compressedMap.get(propertyName);
        if(result == null ){
            return null;
        }
        if(targetResult.isInstance(result)){
            return (T) result;
        }
        throw new IllegalArgumentException("Значение фильтра не соответсткует его представлению");
    }

    private Map<String, Object> compressListValueToMap(){
        if(compresedValues != null){
            return compresedValues;
        }

        compresedValues = new HashMap<>();
        for (JsonSearchQueryFilterValue value : values){
            if(compresedValues.containsKey(value.getPropertyName())){
                if(compresedValues.get(value.getPropertyName()) instanceof List){
                    ((List)compresedValues.get(value.getPropertyName())).add(parseValue(value.getPropertyValue()));
                }else{
                    Object prevValue = compresedValues.get(value.getPropertyName());
                    List tValue = new ArrayList();
                    tValue.add(prevValue);
                    tValue.add(parseValue(value.getPropertyValue()));
                    compresedValues.put(value.getPropertyName(), tValue);
                }
            }else{
                compresedValues.put(value.getPropertyName(), parseValue(value.getPropertyValue()));
            }
        }
        return compresedValues;
    }

    private Object parseValue(Object value){
        Object result = null;
        result = value;

        return result;
    }
}
