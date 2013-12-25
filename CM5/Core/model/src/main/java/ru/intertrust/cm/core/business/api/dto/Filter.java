package ru.intertrust.cm.core.business.api.dto;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

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

    private TreeMap<Integer, Value> parameterMap = new TreeMap<Integer, Value>();
    private TreeMap<Integer, List<Value>> multiParameterMap = new TreeMap<Integer, List<Value>>();

    public void addCriterion(int index, Value value) {
        parameterMap.put(index, value);
    }

    public Set<Integer> getCriterionKeys() {
        return parameterMap.keySet();
    }

    public Value getCriterion(int index) {
        return parameterMap.get(index);
    }

    public void addMultiCriterion(int index, List<Value> value) {
        multiParameterMap.put(index, value);
    }

    public List<Value> getMultiCriterion(int index) {
        return multiParameterMap.get(index);
    }

    public void removeCriterion(int index) {
        parameterMap.remove(index);
        multiParameterMap.remove(index);
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
