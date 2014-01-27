package ru.intertrust.cm.core.config.converter;

import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

import java.util.List;

/**
 * Конвертер для сериализации конфигурации
 * @author vmatsukevich
 *         Date: 7/11/13
 *         Time: 8:26 PM
 */
public class WidgetConfigurationConfigConverter extends ListConverter<WidgetConfigurationConfig> {

    @Override
    public WidgetConfigurationConfig create() {
        return new WidgetConfigurationConfig();
    }

    @Override
    public List<WidgetConfig> getList(WidgetConfigurationConfig configuration) {
        return configuration.getWidgetConfigList();
    }
}
