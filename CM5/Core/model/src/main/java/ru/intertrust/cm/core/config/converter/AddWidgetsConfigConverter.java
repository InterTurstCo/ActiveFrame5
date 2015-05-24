package ru.intertrust.cm.core.config.converter;

import ru.intertrust.cm.core.config.gui.form.extension.widget.configuration.AddWidgetsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.2015
 *         Time: 14:17
 */
public class AddWidgetsConfigConverter extends ListConverter<AddWidgetsConfig> {

    @Override
    public AddWidgetsConfig create() {
        return new AddWidgetsConfig();
    }

    @Override
    public List<WidgetConfig> getList(AddWidgetsConfig configuration) {
        return configuration.getWidgetConfigs();
    }
}