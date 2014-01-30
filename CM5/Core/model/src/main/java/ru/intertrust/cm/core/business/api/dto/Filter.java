package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Date;

/**
 * Фильтр, используемый в основном для отсеивания результатов коллекций
 *
 * Author: Denis Mitavskiy
 * Date: 24.05.13
 * Time: 13:53
 */
public class Filter implements Dto {

    /**
     * содержит имя фильтра
     */
    protected String filter;

    private HashMap<Integer, List<Value>> parameterMap = new HashMap<Integer, List<Value>>();
    private HashMap<Integer, Boolean> isSingleParameterMap = new HashMap<Integer, Boolean>();

    public void addCriterion(int index, Value value) {
        ArrayList<Value> list = new ArrayList<Value>(1);
        list.add(value);
        parameterMap.put(index, list);
        isSingleParameterMap.put(index, Boolean.TRUE);
    }

    public void addStringCriterion(int index, String value) {
        addCriterion(index, new StringValue(value));
    }

    /**
     * Добавляет дату в критерий
     * @param index индекс
     * @param value дата
     */
    public void addDateCriterion(int index, Date value) {
        DateTimeValue dateTimeValue = new DateTimeValue(value);
        addCriterion(index, dateTimeValue);
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

    public Set<Integer> getCriterionKeys() {
        return parameterMap.keySet();
    }

    public Value getCriterion(int index) {
        List<Value> list = parameterMap.get(index);
        if (list == null || isSingleParameterMap.get(index) == Boolean.FALSE) {
            return null;
        }
        return list.get(0);
    }

    public void addMultiCriterion(int index, List<Value> value) {
        parameterMap.put(index, value);
        isSingleParameterMap.put(index, Boolean.FALSE);
    }

    public List<Value> getMultiCriterion(int index) {
        if (isSingleParameterMap.get(index) == Boolean.TRUE) {
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

        if (!parameterMap.equals(another.parameterMap)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parameterMap.hashCode();
    }
}
