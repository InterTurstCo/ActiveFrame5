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
        if (getClazz() == null) {
            return;
        }
        if (!updateEvent.configTypeChanged((Class<? extends TopLevelConfig>) getClass())) {
            return;
        }
        if (updateEvent.isLegacyDevelopmentMechanism()) {
            doUpdate(updateEvent);
        } else {
            onUpdate(updateEvent);
        }
    }

    /**
     * Производит действия по обновлению конфигурации (обновляет конфигурационные кэши и т.п.)
     * @param configurationUpdateEvent
     * @deprecated этот метод используется механизмом "только для разработки" для обновления конфигурации
     */
    protected abstract void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent);

    /**
     * Обработчик события обновления конфигурации. Чаще всего, этот метод переопределять не требуется, так как к моменту его вызова
     * {@link ru.intertrust.cm.core.config.ConfigurationExplorer} и его встроенные кэши уже обновлены. Его нужно переопределять для
     * проведения специфичных действий: очистки кэшей, сброса каких-то дополнительных настроек и прочего.
     * @param event событие обновления конфигурации
     */
    protected void onUpdate(ConfigurationUpdateEvent event) {

    }

    /**
     * Возвращает тип конфигурации, кот. обрабатывает данный обработчик
     */
    protected abstract Class<T> getClazz();
}
