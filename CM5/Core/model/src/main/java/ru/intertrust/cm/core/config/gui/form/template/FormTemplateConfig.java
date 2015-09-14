package ru.intertrust.cm.core.config.gui.form.template;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.08.2015
 *         Time: 12:05
 */
public abstract class FormTemplateConfig implements TopLevelConfig {
    @Attribute
    private String name;

    @Element(name = "widget-config", required = false)
    private WidgetConfigurationConfig widgetConfigurationConfig;

    public String getName() {
        return name;
    }

    public WidgetConfigurationConfig getWidgetConfigurationConfig() {
        return widgetConfigurationConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormTemplateConfig that = (FormTemplateConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (widgetConfigurationConfig != null ? !widgetConfigurationConfig.equals(that.widgetConfigurationConfig)
                : that.widgetConfigurationConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (widgetConfigurationConfig != null ? widgetConfigurationConfig.hashCode() : 0);
        return result;
    }
}
