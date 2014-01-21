package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "label")
public class LabelConfig extends WidgetConfig implements Dto {
    @Element(name = "text", required = false)
    private String text;

    @Element(name = "relates-to", required = false)
    private RelatesToConfig relatesTo;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public RelatesToConfig getRelatesTo() {
        return relatesTo;
    }

    public void setRelatesTo(RelatesToConfig relatesTo) {
        this.relatesTo = relatesTo;
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

        LabelConfig that = (LabelConfig) o;

        if (relatesTo != null ? !relatesTo.equals(that.relatesTo) : that.relatesTo != null) {
            return false;
        }
        if (text != null ? !text.equals(that.text) : that.text != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (relatesTo != null ? relatesTo.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {

        return "label";
    }
}
