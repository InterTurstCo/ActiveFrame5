package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 05.09.13
 *         Time: 15:58
 */
@Root( name = "form")
public class FormConfig implements Dto {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "domain-object-type")
    private String domainObjectType;

    @Attribute(name = "is-default")
    private boolean default_;

    @Element(name = "markup")
    private MarkupConfig markup;

    @Element(name = "widget-template")
    private WidgetConfigurationConfig widgetConfigurationConfig;

    public MarkupConfig getMarkup() {
        return markup;
    }

    public void setMarkup(MarkupConfig markup) {
        this.markup = markup;
    }

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

    public boolean isDefault_() {
        return default_;
    }

    public void setDefault_(boolean default_) {
        this.default_ = default_;
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

        if (default_ != that.default_) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }
        if (markup != null ? !markup.equals(that.markup) : that.markup != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (widgetConfigurationConfig != null ? !widgetConfigurationConfig.equals(that.widgetConfigurationConfig) : that.widgetConfigurationConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        result = result + (default_ ? 1 : 0);
        result = result + (markup != null ? markup.hashCode() : 0);
        result = result + (widgetConfigurationConfig != null ? widgetConfigurationConfig.hashCode() : 0);
        return result;
    }
}
