package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.model.GwtIncompatible;

import java.math.BigDecimal;
import java.util.*;

/**
 * Фильтр, используемый в основном для отсеивания результатов коллекций
 *
 * Author: Denis Mitavskiy
 * Date: 24.05.13
 * Time: 13:53
 */
public class Filter implements Dto, Cloneable {

    /**
     * содержит имя фильтра
     */
    protected String filter;

    protected HashMap<Integer, List<Value>> parameterMap;
    private HashMap<Integer, Boolean> isSingleParameterMap;
    
    public Filter() {
        parameterMap = new HashMap<>();
        isSingleParameterMap = new HashMap<>();
    }

    public Filter(Filter source) {
        this(source, false);
    }

    public Filter(Filter source, boolean makeShallowCopies) {
        this.filter = source.getFilter();
        if (makeShallowCopies) {
            parameterMap = new HashMap<>(source.parameterMap);
            isSingleParameterMap = new HashMap<>(source.isSingleParameterMap);

        } else {
            this.parameterMap = source.getParameterMap();
            this.isSingleParameterMap = source.getIsSingleParameterMap();
        }
    }

    public HashMap<Integer, List<Value>> getParameterMap() {
        return parameterMap;
    }

    public HashMap<Integer, Boolean> getIsSingleParameterMap() {
        return isSingleParameterMap;
    }

    public static Filter create(final String filterName, final int index, final Value filterValue) {
        final Filter result = new Filter();
        result.setFilter(filterName);
        result.addCriterion(index, filterValue);
        return result;
    }

    public void addCriterion(int index, Value value) {
        ArrayList<Value> list = new ArrayList<>(1);
        list.add(value);
        parameterMap.put(index, list);
        isSingleParameterMap.put(index, Boolean.TRUE);
    }

    public void addStringCriterion(int index, String value) {
        addCriterion(index, new StringValue(value));
    }

    public void addMultiStringCriterion(int index, List<String> stringList) {
        if (stringList != null){
            List<Value> values = new ArrayList<>(stringList.size());
            for (String string : stringList) {
                values.add(new StringValue(string));
            }
            addMultiCriterion(index, values);
        }
    }

    public void addBooleanCriterion(int index, Boolean value) {
        addCriterion(index, new BooleanValue(value));
    }

    public void addMultiBooleanCriterion(int index, List<Boolean> booleanList) {
        if (booleanList != null){
            List<Value> values = new ArrayList<>(booleanList.size());
            for (Boolean value : booleanList) {
                values.add(new BooleanValue(value));
            }
            addMultiCriterion(index, values);
        }
    }

    public void addDecimalCriterion(int index, BigDecimal value) {
        addCriterion(index, new DecimalValue(value));
    }

    public void addMultiDecimalCriterion(int index, List<BigDecimal> decimalList) {
        if (decimalList != null){
            List<Value> values = new ArrayList<>(decimalList.size());
            for (BigDecimal value : decimalList) {
                values.add(new DecimalValue(value));
            }
            addMultiCriterion(index, values);
        }
    }

    public void addLongCriterion(int index, Long value) {
        addCriterion(index, new LongValue(value));
    }

    public void addMultiLongCriterion(int index, List<Long> longList) {
        if (longList != null){
            List<Value> values = new ArrayList<>(longList.size());
            for (Long value : longList) {
                values.add(new LongValue(value));
            }
            addMultiCriterion(index, values);
        }
    }

    public void addReferenceCriterion(int index, Id value) {
        addCriterion(index, new ReferenceValue(value));
    }

    public void addMultiReferenceCriterion(int index, List<Id> idList) {
        if (idList != null){
            List<Value> values = new ArrayList<>(idList.size());
            for (Id value : idList) {
                values.add(new ReferenceValue(value));
            }
            addMultiCriterion(index, values);
        }
    }

    /**
     * Добавляет дату в критерий
     * @param index индекс
     * @param value дата
     */
    public void addDateTimeCriterion(int index, Date value) {
        DateTimeValue dateTimeValue = new DateTimeValue(value);
        addCriterion(index, dateTimeValue);
    }

    public void addMultiDateTimeCriterion(int index, List<Date> dateList) {
        if (dateList != null){
            List<Value> values = new ArrayList<>(dateList.size());
            for (Date value : dateList) {
                values.add(new DateTimeValue(value));
            }
            addMultiCriterion(index, values);
        }
    }

    /**
     * Добавляет дату с часовым поясом в критерий
     * @param index индекс
     * @param value дата
     */
    public void addDateTimeWithTimeZoneCriterion(int index, DateTimeWithTimeZone value) {
        DateTimeWithTimeZoneValue dateTimeValue = new DateTimeWithTimeZoneValue(value);
        addCriterion(index, dateTimeValue);
    }

    public void addMultiDateTimeWithTimeZoneCriterion(int index, List<DateTimeWithTimeZone> dateList) {
        if (dateList != null){
            List<Value> values = new ArrayList<>(dateList.size());
            for (DateTimeWithTimeZone value : dateList) {
                values.add(new DateTimeWithTimeZoneValue(value));
            }
            addMultiCriterion(index, values);
        }
    }

    /**
     * Добавляет дату без времени в критерий
     * @param index индекс
     * @param timelessDate дата
     */
    public void addTimelessDateCriterion(int index, TimelessDate timelessDate) {
        TimelessDateValue timelessDateValue = new TimelessDateValue(timelessDate);
        addCriterion(index, timelessDateValue);
    }

    public void addMultiTimelessDateCriterion(int index, List<TimelessDate> dateList) {
        if (dateList != null){
            List<Value> values = new ArrayList<>(dateList.size());
            for (TimelessDate value : dateList) {
                values.add(new TimelessDateValue(value));
            }
            addMultiCriterion(index, values);
        }
    }

    public Set<Integer> getCriterionKeys() {
        return parameterMap.keySet();
    }

    public Value getCriterion(int index) {
        List<Value> list = parameterMap.get(index);
        if (list == null || Boolean.FALSE.equals(isSingleParameterMap.get(index))) {
            return null;
        }
        return list.get(0);
    }

    public void addMultiCriterion(int index, List<Value> value) {
        parameterMap.put(index, value);
        isSingleParameterMap.put(index, Boolean.FALSE);
    }

    public List<Value> getMultiCriterion(int index) {
        if (Boolean.TRUE.equals(isSingleParameterMap.get(index))) {
            return null;
        }
        return parameterMap.get(index);
    }

    public void removeCriterion(int index) {
        parameterMap.remove(index);
        isSingleParameterMap.remove(index);
    }

    public void clear() {
        parameterMap.clear();
        isSingleParameterMap.clear();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Filter another = (Filter) o;

        if (filter == null) {
            if (another.filter != null) {
                return false;
            }
        } else if (!filter.equals(another.filter)) {
            return false;
        }
        
        if (!parameterMap.equals(another.parameterMap)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parameterMap.hashCode();
    }

    @Override
    @GwtIncompatible
    public Filter clone() {
        final Filter clone;
        try {
            return (Filter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public String toString() {
        return "Filter [filter=" + filter + ", parameterMap=" + parameterMap + ", isSingleParameterMap=" + isSingleParameterMap + "]";
    }
}
