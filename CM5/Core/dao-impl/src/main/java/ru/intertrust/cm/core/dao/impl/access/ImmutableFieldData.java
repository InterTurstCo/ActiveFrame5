package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;

/**
 * Структура для хранения информации о типе где объявлено Immutable поле, информации о самом поле и его значение
 * @author larin
 *
 */
public class ImmutableFieldData {
    private Id value;
    private String typeName;
    private ReferenceFieldConfig сonfig;
    
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    public ReferenceFieldConfig getСonfig() {
        return сonfig;
    }
    public void setСonfig(ReferenceFieldConfig сonfig) {
        this.сonfig = сonfig;
    }
    public Id getValue() {
        return value;
    }
    public void setValue(Id value) {
        this.value = value;
    }    
}
