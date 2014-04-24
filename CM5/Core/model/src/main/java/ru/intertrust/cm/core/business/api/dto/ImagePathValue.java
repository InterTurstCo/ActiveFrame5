package ru.intertrust.cm.core.business.api.dto;

/**
 * Created by User on 13.02.14.
 */
public class ImagePathValue extends Value<ImagePathValue> {
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

    @Override
    public int compareTo(ImagePathValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : value.compareTo(o.value);
        }
    }
}
