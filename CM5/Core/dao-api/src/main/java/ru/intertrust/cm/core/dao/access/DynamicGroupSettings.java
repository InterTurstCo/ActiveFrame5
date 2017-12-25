package ru.intertrust.cm.core.dao.access;

/**
 * Сонфигурация сервиса динамических групп
 * @author larin
 *
 */
public interface DynamicGroupSettings {
    /**
     * Возвращает флаг отключенности механизма пересчета групп в плоскую структуру
     * @return
     */
    boolean isDisableGroupUncover();

    /**
     * Устанавливает флаг отключенности механизма пересчета групп в плоскую структуру
     * @param value
     */
    void setDisableGroupUncover(boolean value);

    /**
     * Возвращает флаг отключенности механизма пересчета динамических групп
     * @return
     */
    boolean isDisableGroupCalculation();

    /**
     * Устанавливает флаг отключенности механизма пересчета динамических групп
     * @param value
     */
    void setDisableGroupCalculation(boolean value);
}
