package ru.intertrust.cm.core.business.api.dto;

/**
 * значение поля объекта профиля типа ссылка
 */
public class ProfileReferenceValue extends ReferenceValue implements ProfileValue{

    private boolean readOnly;

    public ProfileReferenceValue() {
        super();
    }

    public ProfileReferenceValue(Id reference) {
        super(reference);
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
