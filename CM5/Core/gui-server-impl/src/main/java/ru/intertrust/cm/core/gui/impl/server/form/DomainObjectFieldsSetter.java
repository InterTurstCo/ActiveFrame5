package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.config.gui.form.widget.UniqueKeyValueConfig;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 27.06.2014
 *         Time: 20:18
 */
public class DomainObjectFieldsSetter {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String TIMELESS_DATE_FORMAT = "yyyy-MM-dd";

    private final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);
    private final SimpleDateFormat TIMELESS_DATE_FORMATTER = new SimpleDateFormat(TIMELESS_DATE_FORMAT);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private IdService idService;

    @Autowired
    private PersonService personService;

    private DomainObject linkedObject;
    private List<FieldValueConfig> fieldValueConfigs;
    private DomainObject baseObject;
    private Date currentMoment;

    public DomainObjectFieldsSetter(final DomainObject domainObject, final List<FieldValueConfig> fieldValueConfigs, final DomainObject baseObject) {
        this.linkedObject = domainObject;
        this.fieldValueConfigs = fieldValueConfigs;
        this.baseObject = baseObject;
        this.currentMoment = new Date();
    }

    public void setFields() {
        for (FieldValueConfig fieldValueConfig : fieldValueConfigs) {
            setFieldValue(fieldValueConfig);
        }
    }

    private void setFieldValue(final FieldValueConfig fieldValueConfig) {
        final String fieldName = fieldValueConfig.getName();
        final FieldConfig fieldConfig = configurationExplorer.getFieldConfig(linkedObject.getTypeName(), fieldName);
        if (fieldValueConfig.isSetNull()) {
            linkedObject.setValue(fieldName, null);
            return;
        }
        if (fieldValueConfig.isSetBaseObject()) {
            linkedObject.setReference(fieldName, baseObject);
            return;
        }
        if (fieldValueConfig.isSetCurrentUser()) {
            linkedObject.setReference(fieldName, personService.getCurrentPerson());
            return;
        }
        if (fieldValueConfig.getUniqueKeyValueConfig() != null) {
            linkedObject.setReference(fieldName, findDomainObjectByUniqueKey(fieldValueConfig.getUniqueKeyValueConfig()));
        }

        final Value value = textToValue(fieldValueConfig, fieldConfig);
        linkedObject.setValue(fieldName, value);
    }

    private DomainObject findDomainObjectByUniqueKey(UniqueKeyValueConfig uniqueKeyValueConfig) {
        final List<FieldValueConfig> fieldValueConfigs = uniqueKeyValueConfig.getFieldValueConfigs();
        final String type = uniqueKeyValueConfig.getType();
        final HashMap<String, Value> uniqueKeyValuesByName = new HashMap<>(fieldValueConfigs.size());
        for (FieldValueConfig fieldValueConfig : fieldValueConfigs) {
            final FieldConfig fieldConfig = configurationExplorer.getFieldConfig(type, fieldValueConfig.getName());
            uniqueKeyValuesByName.put(fieldValueConfig.getName(), textToValue(fieldValueConfig, fieldConfig));
        }

        return null; // todo findByUniqueKey
    }

    private Value textToValue(FieldValueConfig fieldValueConfig, FieldConfig fieldConfig) {
        final String valueText = fieldValueConfig.getValue();
        final Value value;
        switch (fieldConfig.getFieldType()) {
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
                value = getDateTimeWithTimeZone(valueText, fieldValueConfig.isSetCurrentMoment(), fieldValueConfig.getTimeZoneId());
                break;
            case DATETIME:
                value = getDateTime(valueText, fieldValueConfig.isSetCurrentMoment(), fieldValueConfig.getTimeZoneId());
                break;
            case TIMELESSDATE:
                value = getTimelessDate(valueText, fieldValueConfig.isSetCurrentMoment(), fieldValueConfig.getTimeZoneId());
                break;
            case REFERENCE:
                value = getReferenceValue(valueText);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Field Type: " + fieldConfig.getFieldType());
        }
        return value;
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
            return new DateTimeWithTimeZoneValue(currentMoment, usedTimeZone);
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
            return new DateTimeValue(currentMoment);
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
            throw new IllegalArgumentException("Invalid text: " + value);
        }
    }

    private TimelessDateValue getTimelessDate(String value, boolean setCurrentMoment, String timeZoneId) {
        final TimeZone usedTimeZone = timeZoneId == null ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneId);
        if (setCurrentMoment) {
            return new TimelessDateValue(currentMoment, usedTimeZone);
        }
        if (value == null || value.isEmpty()) {
            return new TimelessDateValue();
        }
        try { // default time zone for parsing - it's ok
            TIMELESS_DATE_FORMATTER.setTimeZone(usedTimeZone);
            final Date date = TIMELESS_DATE_FORMATTER.parse(value);
            return new TimelessDateValue(date, usedTimeZone);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid text: " + value);
        }
    }

    private ReferenceValue getReferenceValue(String value) {
        return value == null || value.isEmpty() ? new ReferenceValue() : new ReferenceValue(idService.createId(value));
    }
}
