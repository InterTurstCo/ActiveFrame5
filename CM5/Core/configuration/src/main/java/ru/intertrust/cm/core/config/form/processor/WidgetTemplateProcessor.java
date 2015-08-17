package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.08.2015
 *         Time: 9:35
 */
public interface WidgetTemplateProcessor {
    List<WidgetConfig> processTemplates(String formName, List<WidgetConfig> widgetConfigs);
    boolean hasTemplateBasedWidgets(List<WidgetConfig> widgetConfigs);
}
