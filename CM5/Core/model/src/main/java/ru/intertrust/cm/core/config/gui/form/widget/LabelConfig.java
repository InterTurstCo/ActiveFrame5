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

    @Element(name = "pattern", required = false)
    private PatternConfig pattern;

    @Element(name = "font-weight", required = false)
    private FontWeightConfig fontWeight;

    @Element(name = "font-style", required = false)
    private FontStyleConfig fontStyle;

    @Element(name = "font-size", required = false)
    private FontSizeConfig fontSize;

    @Element(name = "renderer", required = false)
    private RendererConfig renderer;

    @Element(name = "all-values-empty-message", required = false)
    private AllValuesEmptyMessageConfig allValuesEmptyMessage;

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

    public FontWeightConfig getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(FontWeightConfig fontWeight) {
        this.fontWeight = fontWeight;
    }

    public FontStyleConfig getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(FontStyleConfig fontStyle) {
        this.fontStyle = fontStyle;
    }

    public PatternConfig getPattern() {
        return pattern;
    }

    public void setPattern(PatternConfig pattern) {
        this.pattern = pattern;
    }

    public FontSizeConfig getFontSize() {
        return fontSize;
    }

    public void setFontSize(FontSizeConfig fontSize) {
        this.fontSize = fontSize;
    }

    public RendererConfig getRenderer() {
        return renderer;
    }

    public void setRenderer(RendererConfig renderer) {
        this.renderer = renderer;
    }

    public AllValuesEmptyMessageConfig getAllValuesEmptyMessage() {
        return allValuesEmptyMessage;
    }

    public void setAllValuesEmptyMessage(AllValuesEmptyMessageConfig allValuesEmptyMessage) {
        this.allValuesEmptyMessage = allValuesEmptyMessage;
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
        if (renderer != null ? !renderer.equals(that.renderer) : that.renderer != null) {
            return false;
        }
        if (allValuesEmptyMessage != null ? !allValuesEmptyMessage.equals(that.allValuesEmptyMessage) : that.
                allValuesEmptyMessage != null) {
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
        result = 31 * result + (renderer != null ? renderer.hashCode() : 0);
        result = 31 * result + (allValuesEmptyMessage != null ? allValuesEmptyMessage.hashCode() : 0);
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
