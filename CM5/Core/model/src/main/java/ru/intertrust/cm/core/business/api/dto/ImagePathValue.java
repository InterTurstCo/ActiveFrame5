package ru.intertrust.cm.core.business.api.dto;

/**
 * Created by User on 13.02.14.
 */
public class ImagePathValue extends Value {
    private String value;

    /**
     * Создает пустое  значение
     */
    public ImagePathValue() {

    }
    /**
     * Создает строковое значение пути к картинке
     * @param value строковое значение
     */
    public ImagePathValue(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }
}
