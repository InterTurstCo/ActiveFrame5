package ru.intertrust.cm.core.business.api.dto;

/**
 * Интерфейс значения профиля
 * @author larin
 *
 */
public interface ProfileValue {
    /**
     * Получение флага только для чтения
     * @return
     */
    boolean isReadOnly();
    
    /**
     * Установка флага только для чтения
     * @param readOnly
     */
    void setReadOnly(boolean readOnly);
}
