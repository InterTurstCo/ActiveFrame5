package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:28.
 */
@Element(name = "action-ref")
public class ActionRefConfig extends BaseActionConfig {

    @Attribute(name = "name-ref")
    private String nameRef;

    @Attribute(name = "text", required = false)
    @Localizable
    private String text;

    @Attribute(name = "showText", required = false)
    private boolean showText = true;

    @Attribute(name = "showImage", required = false)
    private boolean showImage = true;

    @Attribute(name = "merged", required = false)
    private Boolean merged;

    public String getNameRef() {
        return nameRef;
    }

    public String getText() {
        return text;
    }

    public boolean isShowText() {
        return showText;
    }

    public boolean isShowImage() {
        return showImage;
    }

    public Boolean getMerged() {
        return merged;
    }

    public void setNameRef(String nameRef) {
        this.nameRef = nameRef;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }

    public void setMerged(Boolean merged) {
        this.merged = merged;
    }

    @Override
    public String toString() {
        return new StringBuilder(ActionRefConfig.class.getSimpleName())
                .append(": nameRef=").append(nameRef)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ActionRefConfig that = (ActionRefConfig) o;

        if (showText != that.showText) return false;
        if (showImage != that.showImage) return false;
        if (nameRef != null ? !nameRef.equals(that.nameRef) : that.nameRef != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (merged != null ? !merged.equals(that.merged) : that.merged != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nameRef != null ? nameRef.hashCode() : 0);
        return result;
    }
}
