package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 05.09.13
 *         Time: 15:58
 */
@Root( name = "form", strict = false)
public class FormConfig implements Dto, TopLevelConfig {
    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Attribute(name = "is-default",required = false)
    private boolean isDefault;

    @Attribute(name = "debug",required = false)
    private boolean debug;

    @Attribute(name = "min-width",required = false)
    private String minWidth;

    @Element(name = "markup")
    private MarkupConfig markup;

    @Element(name = "widget-config")
    private ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig widgetConfigurationConfig;

    public MarkupConfig getMarkup() {
        return markup;
    }

    public void setMarkup(MarkupConfig markup) {
        this.markup = markup;
    }
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(String minWidth) {
        this.minWidth = minWidth;
    }

    public WidgetConfigurationConfig getWidgetConfigurationConfig() {
        return widgetConfigurationConfig;
    }

    public void setWidgetConfigurationConfig(WidgetConfigurationConfig widgetConfigurationConfig) {
        this.widgetConfigurationConfig = widgetConfigurationConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormConfig that = (FormConfig) o;

        if (isDefault != that.isDefault) {
            return false;
        }
        if (debug != that.debug) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }
        if (markup != null ? !markup.equals(that.markup) : that.markup != null) {
            return false;
        }
        if (minWidth != null ? !minWidth.equals(that.minWidth) : that.minWidth != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (widgetConfigurationConfig != null ? !widgetConfigurationConfig.equals(that.
                widgetConfigurationConfig) : that.widgetConfigurationConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        result = 31 * result + (isDefault ? 1 : 0);
        result = 31 * result + (debug ? 1 : 0);
        result = 31 * result + (markup != null ? markup.hashCode() : 0);
        result = 31 * result + (minWidth != null ? minWidth.hashCode() : 0);
        result = 31 * result + (widgetConfigurationConfig != null ? widgetConfigurationConfig.hashCode() : 0);
        return result;
    }
}
