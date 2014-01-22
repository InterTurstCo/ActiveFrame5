package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
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

    @Attribute(name = "pattern", required = false)
    private String pattern;

    @Attribute(name = "font-weight", required = false)
    private String fontWeight;

    @Attribute(name = "font-style", required = false)
    private String fontStyle;

    @Attribute(name = "font-size", required = false)
    private String fontSize;

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

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
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

        if (fontSize != null ? !fontSize.equals(that.fontSize) : that.fontSize != null) {
            return false;
        }
        if (fontStyle != null ? !fontStyle.equals(that.fontStyle) : that.fontStyle != null) {
            return false;
        }
        if (fontWeight != null ? !fontWeight.equals(that.fontWeight) : that.fontWeight != null) {
            return false;
        }
        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) {
            return false;
        }
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
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        result = 31 * result + (fontWeight != null ? fontWeight.hashCode() : 0);
        result = 31 * result + (fontStyle != null ? fontStyle.hashCode() : 0);
        result = 31 * result + (fontSize != null ? fontSize.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "label";
    }
}
