package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.impl.utils.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.generateReferenceTypeParameter;

/**
 * Служебный класс для работы с параметризованными запросами напрямую через PreparedStatement.
 * Предоставляет интерфейс для работы с именованными  параметрами.
 */
public class Query {

    private String query;
    private Map<String, ParameterInfo> nameToParameterInfoMap = new HashMap<>();
    private Map<Integer, ParameterInfo> indexToParameterInfo = new HashMap<>();
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
    public Map<String, ParameterInfo> getNameToParameterInfoMap() {
        return nameToParameterInfoMap;
    }

    public Map<Integer, ParameterInfo> getIndexToParameterInfo() {
        return indexToParameterInfo;
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    public int addParameter(String parameter, Class<? extends FieldConfig> type) {
        if (LongFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.LONG);
            return 1;
        } else if (DateTimeFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.DATETIME);
            return 1;
        } else if (ReferenceFieldConfig.class.equals(type)) {
            addReferenceParameters(parameter);
            return 2;
        } else if (PasswordFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.STRING);
            return 1;
        } else if (TimelessDateFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.DATETIME);
            return 1;
        } else if (DateTimeWithTimeZoneFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.DATETIME);
            addParameter(getTimeZoneIdColumnName(parameter), ParameterType.STRING);
            return 2;
        } else if (DecimalFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.DECIMAL);
            return 1;
        } else if (TextFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.TEXT);
            return 1;
        } else if (BooleanFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.BOOLEAN);
            return 1;
        } else if (StringFieldConfig.class.equals(type)) {
            addParameter(parameter, ParameterType.STRING);
            return 1;
        } else {
            throw new IllegalArgumentException("Unsupported field config type '" + type +
                    "' for parameter '" + parameter + "'");
        }
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    public void addLongParameter(String parameter) {
        addParameter(parameter, ParameterType.LONG);
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    public void addReferenceParameter(String parameter) {
        addParameter(parameter, ParameterType.REFERENCE);
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    public void addReferenceTypeParameter(String parameter) {
        addParameter(parameter, ParameterType.REFERENCE_TYPE);
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    public void addReferenceParameters(String parameter) {
        addParameter(parameter, ParameterType.REFERENCE);
        addParameter(generateReferenceTypeParameter(parameter), ParameterType.REFERENCE_TYPE);
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    public void addDateParameter(String parameter) {
        addParameter(parameter, ParameterType.DATETIME);
    }

    /**
     * Добавляет именованный параметр в запрос
     */
    private void addParameter(String parameter, ParameterType type) {
        if (parameter == null) {
            return;
        }

        ParameterInfo parameterInfo = new ParameterInfo(parameter, type, parameterCounter);
        nameToParameterInfoMap.put(parameter, parameterInfo);
        indexToParameterInfo.put(parameterCounter, parameterInfo);

        parameterCounter++;
    }

    /**
     * Добавляет именованные параметры в запрос
     * @param parameters
     */
    public void addParameters(Class<? extends FieldConfig> type, String... parameters) {
        if (parameters == null) {
            return;
        }

        for (String parameter : parameters) {
            addParameter(parameter, type);
        }
    }

    /**
     * Добавляет именованные параметры в запрос
     * @param parameters
     */
    public void addParameters(List<String> parameters, List<FieldConfig> fieldConfigs) {
        if (parameters == null || fieldConfigs == null) {
            return;
        }

        int j = 0;
        for (FieldConfig fieldConfig : fieldConfigs) {
            j += addParameter(parameters.get(j), fieldConfig.getClass());
        }
    }

    static class ParameterInfo {

        private String name;
        private ParameterType type;
        private Integer index;

        ParameterInfo(String name, ParameterType type, Integer index) {
            this.name = name;
            this.type = type;
            this.index = index;
        }

        public ParameterType getType() {
            return type;
        }

        public Integer getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }
    }
}
