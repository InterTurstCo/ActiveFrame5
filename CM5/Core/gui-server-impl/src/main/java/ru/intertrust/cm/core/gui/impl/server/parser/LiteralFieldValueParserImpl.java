package ru.intertrust.cm.core.gui.impl.server.parser;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import static ru.intertrust.cm.core.business.api.dto.util.ModelConstants.DATE_TIME_FORMATTER;
import static ru.intertrust.cm.core.business.api.dto.util.ModelConstants.TIMELESS_DATE_FORMATTER;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 0:27
 */
@ComponentName("literal-parser")
public class LiteralFieldValueParserImpl implements LiteralFieldValueParser {

    @Autowired
    private IdService idService;

    private Value textToValue(String valueText, FieldType fieldType, String timeZoneId, boolean setCurrentMoment) {

        final Value value;
        switch (fieldType) {
            case BOOLEAN:
                value = getBoolean(valueText);
                break;
            case STRING:
                value = getString(valueText);
                break;
            case TEXT:
                value = getString(valueText);
                break;
            case LONG:
                value = getLong(valueText);
                break;
            case DECIMAL:
                value = getDecimal(valueText);
                break;
            case DATETIMEWITHTIMEZONE:
                value = getDateTimeWithTimeZone(valueText, setCurrentMoment, timeZoneId);
                break;
            case DATETIME:
                value = getDateTime(valueText, setCurrentMoment, timeZoneId);
                break;
            case TIMELESSDATE:
                value = getTimelessDate(valueText, setCurrentMoment, timeZoneId);
                break;
            case REFERENCE:
                value = getReferenceValue(valueText);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Field Type: " + fieldType);
        }
        return value;
    }

    public Value textToValue(FieldValueConfig fieldValueConfig, FieldConfig fieldConfig) {
        return textToValue(fieldValueConfig.getValue(), fieldConfig.getFieldType(),
                fieldValueConfig.getTimeZoneId(), fieldValueConfig.isSetCurrentMoment());
    }

    public Value textToValue(String valueText, String fieldTypeStr) {
        FieldType fieldType = FieldType.forTypeName(fieldTypeStr);
        return textToValue(valueText, fieldType);
    }
    public Value getCurrentTimeValue(String fieldTypeStr, String timeZoneId, boolean setCurrentMoment) {
        FieldType fieldType = FieldType.forTypeName(fieldTypeStr);
        return textToValue(null, fieldType, timeZoneId, setCurrentMoment);
    }

    @Override
    public Value textToValue(String valueText, FieldType fieldType) {
        return textToValue(valueText, fieldType, null, false);
    }

    private BooleanValue getBoolean(String value) {
        return new BooleanValue(value == null || value.isEmpty() ? null : "true".equals(value));
    }

    private StringValue getString(String value) {
        return new StringValue(value == null || value.isEmpty() ? null : value);
    }

    private LongValue getLong(String value) {
        return new LongValue(value == null || value.isEmpty() ? null : Long.valueOf(value));
    }

    private DecimalValue getDecimal(String value) {
        return new DecimalValue(value == null || value.isEmpty() ? null : new BigDecimal(value));
    }

    private DateTimeWithTimeZoneValue getDateTimeWithTimeZone(String value, boolean setCurrentMoment, String timeZoneId) {
        final TimeZone usedTimeZone = timeZoneId == null ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneId);
        if (setCurrentMoment) {
            return new DateTimeWithTimeZoneValue(new Date(), usedTimeZone);
        }
        if (value == null || value.isEmpty()) {
            return new DateTimeWithTimeZoneValue();
        }
        try {
            DATE_TIME_FORMATTER.setTimeZone(usedTimeZone);
            final Date date = DATE_TIME_FORMATTER.parse(value);
            return new DateTimeWithTimeZoneValue(date, usedTimeZone);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid text: " + value);
        }
    }

    private DateTimeValue getDateTime(String value, boolean setCurrentMoment, String timeZoneId) {
        if (setCurrentMoment) {
            return new DateTimeValue(new Date());
        }
        if (value == null || value.isEmpty()) {
            return new DateTimeValue();
        }
        try {
            final TimeZone usedTimeZone = timeZoneId == null ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneId);
            DATE_TIME_FORMATTER.setTimeZone(usedTimeZone);
            final Date date = DATE_TIME_FORMATTER.parse(value);
            return new DateTimeValue(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private TimelessDateValue getTimelessDate(String value, boolean setCurrentMoment, String timeZoneId) {
        final TimeZone usedTimeZone = timeZoneId == null ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneId);
        if (setCurrentMoment) {
            return new TimelessDateValue(new Date(), usedTimeZone);
        }
        if (value == null || value.isEmpty()) {
            return new TimelessDateValue();
        }
        try { // default time zone for parsing - it's ok
            TIMELESS_DATE_FORMATTER.setTimeZone(usedTimeZone);
            final Date date = TIMELESS_DATE_FORMATTER.parse(value);
            return new TimelessDateValue(date, usedTimeZone);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private ReferenceValue getReferenceValue(String value) {
        return value == null || value.isEmpty() ? new ReferenceValue() : new ReferenceValue(idService.createId(value));
    }

}
