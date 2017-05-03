package ru.intertrust.cm.core.config.event;

import org.springframework.context.ApplicationEvent;
import ru.intertrust.cm.core.config.ConfigurationExplorer;

import java.util.Set;

/**
 * Событие, обработчик которого должен отработать на единственном узле кластера
 * @author Denis Mitavskiy
 *         Date: 02.05.2017
 *         Time: 19:24
 */
public class SingletonConfigurationUpdateEvent extends ApplicationEvent {
    private ConfigurationUpdateEvent original;

    public SingletonConfigurationUpdateEvent(ConfigurationExplorer source, Set<ConfigChange> configChanges) {
        super(source);
        this.original = new ConfigurationUpdateEvent(source, configChanges);
    }
    
    public SingletonConfigurationUpdateEvent(ConfigurationUpdateEvent original) {
        super(original.getSource());
        this.original = original;
    }

    public ConfigurationUpdateEvent getOriginal() {
        return original;
    }
}
