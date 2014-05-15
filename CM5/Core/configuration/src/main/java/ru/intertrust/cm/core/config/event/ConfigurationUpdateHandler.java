package ru.intertrust.cm.core.config.event;


import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * Абстрактный класс, базовый для классов-обработчиков событий изменения конфигурации
 * @param <T> Тип конфигурации, который обрабатывает обработчик
 */
public abstract class ConfigurationUpdateHandler<T> implements ApplicationListener<ConfigurationUpdateEvent> {

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent updateEvent) {
        TopLevelConfig config = updateEvent.getNewConfig();

        if (config == null || getClazz() == null || !getClazz().isInstance(config)) {
            return;
        }

        doUpdate(updateEvent);
    }

    /**
     * Производит действия по обновлению конфигурации (обновляет конфигурационные кэши и т.п.)
     * @param configurationUpdateEvent
     */
    protected abstract void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent);

    /**
     * Возвращает тип конфигурации, кот. обрабатывает данный обработчик
     */
    protected abstract Class<T> getClazz();
}
