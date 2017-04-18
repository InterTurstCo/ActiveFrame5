package ru.intertrust.cm.core.config.base;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Интерфейс конфигурации верхнего уровня. Каждый класс конфигурации верхнего уровня должен реализовывать данный
 * интерфейс, чтобы было возможно сериализовывать объекты данного класса, а так же получать к ним быстрый доступ в
 * {@link ru.intertrust.cm.core.config.ConfigurationExplorer}.
 * @author vmatsukevich
 *         Date: 7/10/13
 *         Time: 11:33 AM
 */
public interface TopLevelConfig extends Dto {
    enum ExtensionPolicy {
        Runtime,
        None;

        public static ExtensionPolicy fromString(String str) {
            return "runtime".equals(str) ? Runtime : None;
        }
    }

    /**
     * Возвращает название конфигурации
     * @return название конфигурации
     */
    String getName();

    /**
     * Возвращает политику замещения экземпляров тэгов
     * @return политику замещения экземпляров тэгов
     */
    ExtensionPolicy getReplacementPolicy();

    /**
     * Возвращает политику создания новых экземпляров тэгов. Относится к типу тэга целиком, а не к конкретному экземпляру
     * @return политику создания новых экземпляров тэгов
     */
    ExtensionPolicy getCreationPolicy();
}
