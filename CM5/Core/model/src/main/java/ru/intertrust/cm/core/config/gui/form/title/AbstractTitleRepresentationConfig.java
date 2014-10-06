package ru.intertrust.cm.core.config.gui.form.title;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.config.gui.form.AbstractRepresentationConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.10.2014
 *         Time: 18:05
 */
public abstract class AbstractTitleRepresentationConfig extends AbstractRepresentationConfig {
    @Attribute(name = "label-widget-id", required = false)
    private String labelWidgetId;

    public String getLabelWidgetId() {
        return labelWidgetId;
    }

    public void setLabelWidgetId(String labelWidgetId) {
        this.labelWidgetId = labelWidgetId;
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

        AbstractTitleRepresentationConfig that = (AbstractTitleRepresentationConfig) o;

        if (labelWidgetId != null ? !labelWidgetId.equals(that.labelWidgetId) : that.labelWidgetId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (labelWidgetId != null ? labelWidgetId.hashCode() : 0);
        return result;
    }
}
