package ru.intertrust.cm.core.business.api.dto;

import java.util.HashMap;

/**
 * Класс описывает ограничения, которые проверяются при клиентской валидации.
 * Состоит из типа валидации (simple, length, range etc) и параметров(например, начало и конец диапазона).
 * @author Lesia Puhova
 *         Date: 26.02.14
 *         Time: 16:32
 */
public class Constraint implements Dto {
    // типы валидации
    public static enum TYPE {
        SIMPLE,
        LENGTH,
        RANGE,
        SCALE_PRECISION;
    }

    // названия параметров валидации
    public static final String PARAM_PATTERN = "pattern";
    public static final String PARAM_LENGTH = "length";
    public static final String PARAM_MIN_LENGTH = "min-length";
    public static final String PARAM_MAX_LENGTH = "max-length";
    public static final String PARAM_RANGE_START = "range-start";
    public static final String PARAM_RANGE_END = "range-end";
    public static final String PARAM_SCALE = "scale";
    public static final String PARAM_PRECISION = "precision";

    // ключевые слова, используемые при простой валидации (SimpleValidator)
    public static final String KEYWORD_NOT_EMPTY = "validate.not-empty";
    public static final String KEYWORD_INTEGER = "validate.integer";
    public static final String KEYWORD_POSITIVE_INT = "validate.positive-int";
    public static final String KEYWORD_DECIMAL = "validate.decimal";
    public static final String KEYWORD_POSITIVE_DEC = "validate.positive-dec";

    private TYPE type;
    private HashMap<String, String> params;

    public Constraint(){
    }

    public Constraint(TYPE type, HashMap<String, String> params) {
        this.type = type;
        this.params = params;
    }

    public TYPE getType() {
        return type;
    }

    public String param(String paramName) {
        return params.get(paramName);
    }
}
