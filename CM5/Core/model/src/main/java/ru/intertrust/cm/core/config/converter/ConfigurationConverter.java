package ru.intertrust.cm.core.config.converter;

import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.List;

/**
 * Конвертер для сериализации конфигурации
 * @author vmatsukevich
 *         Date: 7/11/13
 *         Time: 8:26 PM
 */
public class ConfigurationConverter extends ListConverter<Configuration> {

    @Override
    public Configuration create() {
        return new Configuration();
    }

    @Override
    public List<TopLevelConfig> getList(Configuration configuration) {
        return configuration.getConfigurationList();
    }
}
