package ru.intertrust.cm.core.gui.impl.server.parser;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.util.GuiConstants;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 0:27
 */
//@ComponentName("literal-parser")
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
        final TimeZone usedTimeZone = timeZoneId == null ? ThreadSafeDateFormat.DEFAULT_TIME_ZONE : TimeZone.getTimeZone(timeZoneId);
        if (setCurrentMoment) {
            return new DateTimeWithTimeZoneValue(new Date(), usedTimeZone);
        }
        if (value == null || value.isEmpty()) {
            return new DateTimeWithTimeZoneValue();
        }
        try {
            final Date date = ThreadSafeDateFormat.parse(value, GuiConstants.DATE_TIME_FORMAT, usedTimeZone);
            return new DateTimeWithTimeZoneValue(date, usedTimeZone);
        } catch (Exception e) {
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
            final TimeZone usedTimeZone = timeZoneId == null ? ThreadSafeDateFormat.DEFAULT_TIME_ZONE : TimeZone.getTimeZone(timeZoneId);
            final Date date = ThreadSafeDateFormat.parse(value, GuiConstants.DATE_TIME_FORMAT, usedTimeZone);
            return new DateTimeValue(date);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private TimelessDateValue getTimelessDate(String value, boolean setCurrentMoment, String timeZoneId) {
        final TimeZone usedTimeZone = timeZoneId == null ? ThreadSafeDateFormat.DEFAULT_TIME_ZONE : TimeZone.getTimeZone(timeZoneId);
        if (setCurrentMoment) {
            return new TimelessDateValue(new Date(), usedTimeZone);
        }
        if (value == null || value.isEmpty()) {
            return new TimelessDateValue();
        }
        try { // default time zone for parsing - it's ok
            final Date date = ThreadSafeDateFormat.parse(value, GuiConstants.TIMELESS_DATE_FORMAT, usedTimeZone);
            return new TimelessDateValue(date, usedTimeZone);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private ReferenceValue getReferenceValue(String value) {
        return value == null || value.isEmpty() ? new ReferenceValue() : new ReferenceValue(idService.createId(value));
    }

}
