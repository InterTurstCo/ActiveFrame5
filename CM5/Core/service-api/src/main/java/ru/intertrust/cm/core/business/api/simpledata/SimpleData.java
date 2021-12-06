package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.*;

public class SimpleData implements Dto{
    private String type;
    private String id = UUID.randomUUID().toString();
    private final Map<String, List<Value<?>>> values = new HashMap<>();

    public SimpleData() {
    }

    public SimpleData(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Замена старых значений новыми
     */
    public void setValues(String name, Value<?> ... values){
        List<?> fieldValues = this.values.get(name.toLowerCase());
        if (fieldValues != null){
            fieldValues.clear();
        }
        addValues(name.toLowerCase(), values);
    }

    /**
     * Добавление значение поля
     */
    public void addValues(String name, Value<?> ... values){
        List<Value<?>> fieldValues = this.values.computeIfAbsent(name.toLowerCase(), k -> new ArrayList<>());
        if (values != null) {
            Collections.addAll(fieldValues, values);
        }
    }

    public void deleteValues(String name){
        List<?> fieldValues = this.values.get(name.toLowerCase());
        fieldValues.clear();
    }

    public List<Value<?>> getValues(String name){
        return this.values.get(name.toLowerCase());
    }

    public void setString(String name, String ... values){
        setValues(name, toValues(values, StringValue.class));
    }

    public List<String> getString(String name){
        return fromValues(getValues(name), String.class);
    }

    public void setLong(String name, Long ... values){
        setValues(name, toValues(values, LongValue.class));
    }

    public List<Long> getLong(String name){
        return fromValues(getValues(name), Long.class);
    }

    public void setLong(String name, Integer ... values){
        setValues(name, toValues(values, LongValue.class));
    }

    public void setTimelessDate(String name, TimelessDate ... values){
        setValues(name, toValues(values, TimelessDateValue.class));
    }

    public List<Date> getDateTime(String name){
        return fromValues(getValues(name), Date.class);
    }

    public void setBoolean(String name, Boolean ... values){
        setValues(name, toValues(values, BooleanValue.class));
    }

    public List<Boolean> getBoolean(String name){
        return fromValues(getValues(name), Boolean.class);
    }

    public void setDateTime(String name, Date ... values){
        setValues(name, toValues(values, DateTimeValue.class));
    }

    public List<TimelessDate> getTimelessDate(String name){
        return fromValues(getValues(name), TimelessDate.class);
    }

    private <T> List<T> fromValues(List<Value<?>> values, Class<T> listType) {
        List<T> result = new ArrayList<>(values.size());
        for (Value<?> value : values) {
            final T val = listType.cast(value.get());
            result.add(val);
        }
        return result;
    }

    private Value<?>[] toValues(Object[] values, Class<? extends Value<?>> clazz){
        Value<?>[] result = new Value[values.length];
        for (int i=0; i<values.length; i++) {
            result[i] = toValue(values[i], clazz);
        }
        return result;
    }

    private Value<?> toValue(Object value, Class<? extends Value<?>> clazz){
        Value<?> result;
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
                throw new UnsupportedOperationException("Not support field with type " + clazz);
            }
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
