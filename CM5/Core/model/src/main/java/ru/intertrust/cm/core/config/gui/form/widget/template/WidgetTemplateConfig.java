package ru.intertrust.cm.core.config.gui.form.widget.template;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.converter.WidgetTemplateConverter;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 16/9/13
 *         Time: 12:05 PM
 */
@Root(name = "widget-template")
@Convert(WidgetTemplateConverter.class)
public class WidgetTemplateConfig implements TopLevelConfig {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Element(type=WidgetConfig.class)
    private WidgetConfig widgetConfig;

    @Override
    public String getName() {
        return name != null ? name : "widget-template-config";
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WidgetConfig getWidgetConfig() {
        return widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetTemplateConfig that = (WidgetTemplateConfig) o;

        if (widgetConfig != null ? !widgetConfig.equals(that.widgetConfig) : that.widgetConfig != null) {
            return false;
        }

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        return 31 * result + (widgetConfig != null ? widgetConfig.hashCode() : 0);
    }
}
