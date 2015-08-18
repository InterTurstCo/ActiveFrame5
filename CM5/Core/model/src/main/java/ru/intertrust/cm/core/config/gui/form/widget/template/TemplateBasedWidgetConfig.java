package ru.intertrust.cm.core.config.gui.form.widget.template;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.08.2015
 *         Time: 10:14
 */
@Root(name = TemplateBasedWidgetConfig.COMPONENT_NAME)
public class TemplateBasedWidgetConfig extends WidgetConfig{
    public static final String COMPONENT_NAME = "template-based-widget";

    @Attribute(name = "template-name")
    private String templateName;

    @Element(name = "override", required = false)
    private OverrideConfig overrideConfig;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public OverrideConfig getOverrideConfig() {
        return overrideConfig;
    }

    public void setOverrideConfig(OverrideConfig overrideConfig) {
        this.overrideConfig = overrideConfig;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        TemplateBasedWidgetConfig that = (TemplateBasedWidgetConfig) o;

        if (templateName != null ? !templateName.equals(that.templateName) : that.templateName != null) {
            return false;
        }
        if (overrideConfig != null ? !overrideConfig.equals(that.overrideConfig) : that.overrideConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (templateName != null ? templateName.hashCode() : 0);
        return result;
    }
}
