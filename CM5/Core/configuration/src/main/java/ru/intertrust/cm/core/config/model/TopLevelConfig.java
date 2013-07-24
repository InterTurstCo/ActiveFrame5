package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

/**
 * Интерфейс конфигурации верхнего уровня. Каждый класс конфигурации верхнего уровня должен реализовывать данный
 * интерфейс, чтобы было возможно сериализовывать объекты данного класса, а так же получать к ним быстрый доступ в
 * {@link ru.intertrust.cm.core.config.ConfigurationExplorer}
 * @author vmatsukevich
 *         Date: 7/10/13
 *         Time: 11:33 AM
 */
public interface TopLevelConfig extends Serializable {

    /**
     * Возвращает название конфигурации
     * @return название конфигурации
     */
    String getName();
}
