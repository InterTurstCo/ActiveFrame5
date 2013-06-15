package ru.intertrust.cm.core.business.api.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Фильтр, используемый в основном для отсеивания результатов коллекций
 *
 * Author: Denis Mitavskiy
 * Date: 24.05.13
 * Time: 13:53
 */
public class Filter {
    
    /**
     * содержит имя фильтра
     */
    private String filter;

    Map<Integer, Object> parameterMap = new TreeMap<>();

    public void addCriterion(int index, Value value) {
        parameterMap.put(index, value);
    }

    public Set<Integer> getCriterionKeys() {
        return parameterMap.keySet();
    }

    public Value getCriterion(int index) {
        Object result = parameterMap.get(index);
        if (result instanceof Value) {
            return (Value) result;
        }
        return null;
    }

    public void addMultiCriterion(int index, List<Value> value) {
        parameterMap.put(index, value);
    }

    public List<Value> getMultiCriterion(int index) {
        Object result = parameterMap.get(index);
        if (result instanceof List<?>) {
            return (List<Value>) result;
        }
        return null;
    }

    public void removeCriterion(int index) {
        parameterMap.remove(index);
    }

    public void clear() {
        parameterMap.clear();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

}
