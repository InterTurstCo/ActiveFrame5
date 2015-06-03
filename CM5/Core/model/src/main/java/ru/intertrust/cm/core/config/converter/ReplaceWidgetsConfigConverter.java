package ru.intertrust.cm.core.config.converter;

import ru.intertrust.cm.core.config.gui.form.extension.widget.configuration.ReplaceWidgetsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.2015
 *         Time: 14:49
 */
public class ReplaceWidgetsConfigConverter extends ListConverter<ReplaceWidgetsConfig> {

    @Override
    public ReplaceWidgetsConfig create() {
        return new ReplaceWidgetsConfig();
    }

    @Override
    public List<WidgetConfig> getList(ReplaceWidgetsConfig configuration) {
        return configuration.getWidgetConfigs();
    }
}