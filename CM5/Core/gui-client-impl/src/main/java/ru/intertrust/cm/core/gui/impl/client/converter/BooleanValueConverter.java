package ru.intertrust.cm.core.gui.impl.client.converter;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;

import java.util.HashMap;

/**
 * @author Sergey.Okolot
 *         Created on 21.01.14 11:09.
 */
public class BooleanValueConverter implements ValueConverter<BooleanValue> {
    private static final String TRUE_STR_VALUE = "Да";
    private static final String FALSE_STR_VALUE = "Нет";
    @Override
    public BooleanValue stringToValue(String asString) {
        Boolean result = null;
        final String upperValueStr = asString.toUpperCase();
        if (asString != null) {
            if (TRUE_STR_VALUE.toUpperCase().equals(upperValueStr)) {
                result = Boolean.TRUE;
            } else if (FALSE_STR_VALUE.toUpperCase().equals(upperValueStr)) {
                result = Boolean.FALSE;
            } else {
                throw new ValueConverterException("Illegal value of boolean " + asString + " .Correct values are "
                    + TRUE_STR_VALUE + " or " + FALSE_STR_VALUE);
            }
        }
        return new BooleanValue(result);
    }

    @Override
    public String valueToString(BooleanValue value) {
        final String result;
        if (value == null || value.get() == null) {
            result = "";
        } else {
            result = value.get() ? TRUE_STR_VALUE : FALSE_STR_VALUE;
        }
        return result;
    }

    @Override
    public void init(HashMap<String, Object> params) {
    }
}
