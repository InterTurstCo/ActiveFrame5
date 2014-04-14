package ru.intertrust.cm.core.business.api.dto;

/**
 * Логическое значение поля объекта профиля
 */
public class ProfileBooleanValue extends BooleanValue implements ProfileValue{

    private boolean readOnly;

    public ProfileBooleanValue() {
        super();
    }

    public ProfileBooleanValue(Boolean value) {
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
