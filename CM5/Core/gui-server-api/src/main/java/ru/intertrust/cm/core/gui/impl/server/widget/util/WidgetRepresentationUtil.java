package ru.intertrust.cm.core.gui.impl.server.widget.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.gui.form.widget.BooleanFormatConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NumberFormatConfig;
import ru.intertrust.cm.core.gui.impl.server.widget.DateTimeValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.DateTimeWithTimezoneValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.DateValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.TimelessDateValueConverter;
import ru.intertrust.cm.core.gui.model.DateTimeContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.09.2014
 *         Time: 12:33
 */
public class WidgetRepresentationUtil {
    private static final char ESCAPE_CHAR = '\\';

    public static String getDisplayValue(String fieldName, Value value, FormattingConfig formattingConfig) {
        StringBuilder displayValue = new StringBuilder();
        if (value != null) {
            Object primitiveValue = value.get();
            if (value instanceof DecimalValue || value instanceof LongValue) {
                displayValue.append(getNumberDisplayValue(fieldName, primitiveValue, formattingConfig));
            } else if (primitiveValue != null) {
                SimpleDateFormat dateFormatter = prepareSimpleDateFormat(fieldName, formattingConfig);
                if (value instanceof DateTimeValue) {
                    DateValueConverter<DateTimeValue> dateTimeValueDateValueConverter = new DateTimeValueConverter();
                    String timeZoneId = getTimeZoneId(fieldName, formattingConfig);
                    DateTimeContext dateTimeContext = dateTimeValueDateValueConverter
                            .valueToContext((DateTimeValue) value, timeZoneId, dateFormatter);
                    displayValue.append(dateTimeContext.getDateTime());
                } else if (value instanceof TimelessDateValue) {

                    DateValueConverter<TimelessDateValue> dateValueConverter = new TimelessDateValueConverter();
                    String timeZoneId = getTimeZoneId(fieldName, formattingConfig);
                    DateTimeContext dateTimeContext = dateValueConverter.valueToContext((TimelessDateValue) value,
                            timeZoneId, dateFormatter);
                    displayValue.append(dateTimeContext.getDateTime());
                } else if (value instanceof DateTimeWithTimeZoneValue) {
                    DateValueConverter<DateTimeWithTimeZoneValue> dateValueConverter = new DateTimeWithTimezoneValueConverter();
                    String timeZoneId = getTimeZoneId(fieldName, formattingConfig);
                    DateTimeContext dateTimeContext = dateValueConverter.valueToContext((DateTimeWithTimeZoneValue) value,
                            timeZoneId, dateFormatter);
                    displayValue.append(dateTimeContext.getDateTime());

                } else if (value instanceof BooleanValue) {
                    displayValue.append(getBooleanDisplayValue(fieldName, (BooleanValue) value, formattingConfig));
                } else if (value instanceof ReferenceValue) {
                    ReferenceValue idValue = (ReferenceValue) value;
                    displayValue.append(idValue.get().toStringRepresentation());
                } else {
                    displayValue.append(primitiveValue.toString());
                }
            }
        }

        return displayValue.toString();
    }

    private static String getTimeZoneId(String field, FormattingConfig formattingConfig) {
        if (formattingConfig == null || formattingConfig.getDateFormatConfig() == null
                || formattingConfig.getDateFormatConfig().getTimeZoneConfig() == null
                || !isFormatterUsedForCurrentField(field, formattingConfig.getDateFormatConfig().getFieldsPathConfig())) {
            return ModelUtil.DEFAULT_TIME_ZONE_ID;
        }
        return formattingConfig.getDateFormatConfig().getTimeZoneConfig().getId();
    }

    private static SimpleDateFormat prepareSimpleDateFormat(String field, FormattingConfig formattingConfig) {
        if (formattingConfig != null && formattingConfig.getDateFormatConfig() != null &&
                isFormatterUsedForCurrentField(field, formattingConfig.getDateFormatConfig().getFieldsPathConfig())) {
            return ThreadSafeDateFormat.getDateFormat(new Pair<String, Locale>(formattingConfig.getDateFormatConfig().getPattern(), null), null);
        }
        return ThreadSafeDateFormat.getDateFormat(new Pair<String, Locale>(ModelUtil.DEFAULT_DATE_PATTERN, null), null);
    }

    private static String getNumberDisplayValue(String field, Object primitiveValue, FormattingConfig formattingConfig) {
        String value = formattingConfig == null ? getNumberDisplayWithoutFormatter(primitiveValue)
                : getNumberDisplayValueProbablyWithFormatting(field, primitiveValue, formattingConfig.getNumberFormatConfig());
        return value;

    }

    private static String getNumberDisplayValueProbablyWithFormatting(String field, Object primitiveValue,
                                                                      NumberFormatConfig numberFormatConfig) {
        String value = isFormatterUsedForCurrentField(field, numberFormatConfig.getFieldsPathConfig()) ?
                getNumberDisplayWithFormatting(primitiveValue, numberFormatConfig) :
                getNumberDisplayWithoutFormatter(primitiveValue);
        return value;
    }

    private static String getNumberDisplayWithoutFormatter(Object primitiveValue) {
        String value = primitiveValue == null ? "" : primitiveValue.toString();
        return value;
    }

    private static String getNumberDisplayWithFormatting(Object primitiveValue, NumberFormatConfig numberFormatConfig) {
        DecimalFormat decimalFormat = new DecimalFormat(numberFormatConfig.getPattern());
        String value = primitiveValue == null ? decimalFormat.format(0) : decimalFormat.format(primitiveValue);
        return value;
    }

    private static String getBooleanDisplayValue(String field, BooleanValue primitiveValue, FormattingConfig formattingConfig) {
        String value = formattingConfig == null ? getBooleanDisplayWithoutFormatter(primitiveValue)
                : getBooleanDisplayValueProbablyWithFormatting(field, primitiveValue, formattingConfig.getBooleanFormatConfig());
        return value;

    }

    private static String getBooleanDisplayValueProbablyWithFormatting(String field, BooleanValue primitiveValue,
                                                                       BooleanFormatConfig booleanFormatConfig) {
        return isFormatterUsedForCurrentField(field, booleanFormatConfig.getFieldsPathConfig())
                ? getBooleanDisplayWithFormatting(primitiveValue, booleanFormatConfig)
                : getBooleanDisplayWithoutFormatter(primitiveValue);

    }

    private static String getBooleanDisplayWithoutFormatter(BooleanValue primitiveValue) {
        return primitiveValue == null ? "не указано" : getInitializedBooleanDefaultDisplayValue(primitiveValue);

    }

    private static String getInitializedBooleanDefaultDisplayValue(BooleanValue primitiveValue) {
        return primitiveValue.get() ? "да" : "нет";
    }

    private static String getBooleanDisplayWithFormatting(BooleanValue primitiveValue, BooleanFormatConfig booleanFormatConfig) {
        String value = primitiveValue == null ? booleanFormatConfig.getNullText()
                : getInitializedBooleanDisplayValue(primitiveValue, booleanFormatConfig);
        return value;
    }

    private static String getInitializedBooleanDisplayValue(BooleanValue primitiveValue, BooleanFormatConfig booleanFormatConfig) {
        return primitiveValue.get() ? booleanFormatConfig.getTrueText() : booleanFormatConfig.getFalseText();
    }

    private static boolean isFormatterUsedForCurrentField(String field, FieldPathsConfig fieldPathsConfig) {
        List<FieldPathConfig> fieldPathConfigs = fieldPathsConfig.getFieldPathConfigsList();
        for (FieldPathConfig fieldPathConfig : fieldPathConfigs) {
            if (fieldPathConfig.getValue().equalsIgnoreCase(field)) {
                return true;
            }
        }
        return false;
    }

    public static String getDisplayValue(String fieldName, IdentifiableObject identifiableObject, FormattingConfig formattingConfig) {
        Value value = "id".equalsIgnoreCase(fieldName) ? new ReferenceValue(identifiableObject.getId()) : identifiableObject.getValue(fieldName);
        return getDisplayValue(fieldName, value, formattingConfig);

    }

    public static String escapeSpecialCharacters(String displayValue) {
        StringBuilder sb = new StringBuilder();
        if (displayValue != null) {
            for (int i = 0; i < displayValue.length(); i++) {
                char c = displayValue.charAt(i);
                if (c == ESCAPE_CHAR) {
                    sb.append(ESCAPE_CHAR);
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }


}
