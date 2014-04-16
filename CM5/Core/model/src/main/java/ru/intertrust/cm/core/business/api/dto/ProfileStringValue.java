package ru.intertrust.cm.core.business.api.dto;

/**
 * Строковое значение поля объекта профиля
 */
public class ProfileStringValue extends StringValue implements ProfileValue{

    private boolean readOnly;

    public ProfileStringValue() {
        super();
    }

    public ProfileStringValue(String value) {
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
