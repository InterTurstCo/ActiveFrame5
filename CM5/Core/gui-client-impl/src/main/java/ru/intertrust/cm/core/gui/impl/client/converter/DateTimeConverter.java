package ru.intertrust.cm.core.gui.impl.client.converter;

import com.google.gwt.i18n.client.DateTimeFormat;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Sergey.Okolot
 *         Created on 21.01.14 10:52.
 */
public class DateTimeConverter implements ValueConverter {
    private static final String DEFAULT_PATTERN = "dd.MM.yyyy";
    private DateTimeFormat formatter;

    @Override
    public Value stringToValue(String asString) {
        if (asString == null || asString.isEmpty()) {
            return null;
        } else {
            Date date;
            try {
                date = formatter.parseStrict(asString);
            } catch (IllegalArgumentException iaex) {
                throw new ValueConverterException(iaex.getMessage());
            }
            return new DateTimeValue(date);
        }
    }

    @Override
    public String valueToString(Value value) {
        if (value == null || value.get() == null) {
            return "";
        } else {
            final Date date = (Date) value.get();
            return formatter.format(date);
        }
    }

    @Override
    public void init(HashMap params) {
        final String pattern = (String) params.get("pattern");
        formatter = DateTimeFormat.getFormat(pattern == null ? DEFAULT_PATTERN : pattern);
    }
}
