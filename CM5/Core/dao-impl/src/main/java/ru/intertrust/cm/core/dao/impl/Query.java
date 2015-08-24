package ru.intertrust.cm.core.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Служебный класс для работы с параметризованными запросами напрямую через PreparedStatement.
 * Предоставляет интерфейс для работы с именованными  параметрами.
 */
public class Query {

    private String query;
    private Map<String, List<Integer>> parameterIndexMap = new HashMap<>();
    private int parameterCounter = 1;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Возвращает карту соответствия имен параметров их позициям в запросе
     * @return
     */
    public Map<String, List<Integer>> getParameterIndexMap() {
        return parameterIndexMap;
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    public void addParameter(String parameter) {
        if (parameter == null) {
            return;
        }

        List<Integer> indexes = parameterIndexMap.get(parameter);
        if (indexes == null) {
            indexes = new LinkedList<>();
            parameterIndexMap.put(parameter, indexes);
        }
        indexes.add(parameterCounter++);
    }

    /**
     * Добавляет именованные параметры в запрос
     * @param parameters
     */
    public void addParameters(String... parameters) {
        if (parameters == null) {
            return;
        }

        for (String parameter : parameters) {
            addParameter(parameter);
        }
    }

    /**
     * Добавляет именованные параметры в запрос
     * @param parameters
     */
    public void addParameters(List<String> parameters) {
        if (parameters == null) {
            return;
        }

        for (String parameter : parameters) {
            addParameter(parameter);
        }
    }
}
