package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.*;

public class SimpleData {
    private String type;
    private Map<String, List<Value>> values;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Замена старых значений новыми
     * @param name
     * @param values
     */
    public void setValues(String name, Value ... values){
        List fieldValues = this.values.get(name.toLowerCase());
        if (fieldValues != null){
            fieldValues.clear();
        }
        addValues(name.toLowerCase(), values);
    }

    /**
     * Добавление значение поля
     * @param name
     * @param values
     */
    public void addValues(String name, Value ... values){
        List fieldValues = this.values.get(name.toLowerCase());
        if (fieldValues == null){
            fieldValues = new ArrayList();
            this.values.put(name.toLowerCase(), fieldValues);
        }
        for (Value value : values) {
            fieldValues.add(value);
        }
    }

    public void deleteValues(String name){
        List fieldValues = this.values.get(name.toLowerCase());
        fieldValues.clear();
    }

    public List<Value> getValues(String name){
        return this.values.get(name.toLowerCase());
    }

    public void setString(String name, String ... values){
        setValues(name, toValues(values, StringValue.class));
    }

    private Value[] toValues(Object[] values, Class<? extends Value> clazz){
        Value[] result = new Value[values.length];
        for (int i=0; i<values.length; i++) {
            result[i] = toValue(values[i], clazz);
        }
        return result;
    }

    private Value toValue(Object value, Class<? extends Value> clazz){
        Value result = null;
            if (clazz.equals(StringValue.class)){
                result = new StringValue(value.toString());
            }else if(clazz.equals(LongValue.class)){
                if (value instanceof Long) {
                    result = new LongValue((Long)value);
                }else if(value instanceof Integer){
                    result = new LongValue((Integer)value);
                }else{
                    throw new UnsupportedOperationException("Can not write " + value.getClass() + " value to Long field");
                }
            }else if(clazz.equals(BooleanValue.class)) {
                result = new BooleanValue((Boolean)value);
            }else if(clazz.equals(DateTimeValue.class)) {
                result = new DateTimeValue((Date)value);
            }else if(clazz.equals(TimelessDateValue.class)) {
                result = new TimelessDateValue((TimelessDate)value);
            }else{
                throw new UnsupportedOperationException("Not support field with type " + clazz.toString());
            }
        return result;
    }
}
