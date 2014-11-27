package ru.intertrust.cm.core.business.api.dto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс описывает ограничения, которые проверяются при клиентской валидации.
 * Состоит из типа валидации (simple, length, range etc) и параметров(например, начало и конец диапазона).
 * @author Lesia Puhova
 *         Date: 26.02.14
 *         Time: 16:32
 */
public class Constraint implements Dto {
    // типы валидации
    public static enum Type {
        SIMPLE,
        LENGTH,
        INT_RANGE,
        DECIMAL_RANGE,
        DATE_RANGE,
        SCALE_PRECISION;
    }

    // названия параметров валидации
    public static final String PARAM_PATTERN = "pattern";
    public static final String PARAM_LENGTH = "length";
    public static final String PARAM_MIN_LENGTH = "min-length";
    public static final String PARAM_MAX_LENGTH = "max-length";
    public static final String PARAM_RANGE_START = "range-start";
    public static final String PARAM_RANGE_END = "range-end";
    public static final String PARAM_RANGE_START_DATE_MS = "range-start-date-ms";
    public static final String PARAM_RANGE_END_DATE_MS = "range-end-date-ms";
    public static final String PARAM_SCALE = "scale";
    public static final String PARAM_PRECISION = "precision";
    public static final String PARAM_FIELD_NAME = "field-name";

    // ключевые слова, используемые при простой валидации (SimpleValidator)
    public static final String KEYWORD_NOT_EMPTY = "not-empty";
    public static final String KEYWORD_INTEGER = "integer";
    public static final String KEYWORD_POSITIVE_INT = "positive-int";
    public static final String KEYWORD_DECIMAL = "decimal";
    public static final String KEYWORD_POSITIVE_DEC = "positive-dec";

    private static final List<String> KEYWORDS = Arrays.asList(KEYWORD_NOT_EMPTY, KEYWORD_INTEGER, KEYWORD_POSITIVE_INT,
        KEYWORD_DECIMAL, KEYWORD_POSITIVE_DEC);

    public static final String VAlUE = "value";

    // params for Server-Side validation
    public static final String PARAM_WIDGET_ID = "widget-id";
    public static final String PARAM_DOMAIN_OBJECT_TYPE = "domain-object-type";

    private Type type;
    private HashMap<String, String> params;

    public Constraint(){ //default constructor added only to conform to serialization. Normally should not be used.
    }

    public Constraint(Type type, HashMap<String, String> params) {
        this.type = type;
        this.params = params;
    }

    public Type getType() {
        return type;
    }

    public Map<String, String> getParams() {
        return  new HashMap(params);
    }

    public String param(String paramName) {
        return params.get(paramName);
    }

    public void addParam(String paramName, String paramValue) {
        params.put(paramName, paramValue);
    }

    @Override
    public String toString() {
        return this.type + " " + params;
    }

    public boolean isApplicableInSearchAndReportForm() {
       if (type == Type.SIMPLE
                && Constraint.KEYWORD_NOT_EMPTY.equals(params.get(Constraint.PARAM_PATTERN))) {
           return false;
       }
        if (type == Type.SIMPLE && !Constraint.KEYWORDS.contains(params.get(Constraint.PARAM_PATTERN))) {
            // free-formed pattern
            return false;
        }
        if (type == Type.LENGTH && (params.get(Constraint.PARAM_MIN_LENGTH) != null ||
                params.get(Constraint.PARAM_LENGTH) != null)) {
            return false;
        }

        return true;
    }
}
