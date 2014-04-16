package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * значение поля объекта профиля типа Дата/время
 */
public class ProfileDateTimeValue extends DateTimeValue implements ProfileValue{

    private boolean readOnly;

    public ProfileDateTimeValue() {
        super();
    }

    public ProfileDateTimeValue(Date value) {
        super(value);
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
