package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.title.TitleConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "linked-form")
public class LinkedFormConfig implements Dto{
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "inline", required = false)
    private boolean inline;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Attribute(name = "modal-width", required = false)
    private String modalWidth;

    @Attribute(name = "modal-height", required = false)
    private String modalHeight;

    @Attribute(name = "resizable", required = false)
    private boolean resizable;

    @Element(name = "title", required = false)
    private TitleConfig titleConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public TitleConfig getTitleConfig() {
        return titleConfig;
    }

    public void setTitleConfig(TitleConfig titleConfig) {
        this.titleConfig = titleConfig;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public String getModalWidth() {
        return modalWidth;
    }

    public String getModalHeight() {
        return modalHeight;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public void setModalWidth(String modalWidth) {
        this.modalWidth = modalWidth;
    }

    public void setModalHeight(String modalHeight) {
        this.modalHeight = modalHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkedFormConfig that = (LinkedFormConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (inline!=that.inline) {
            return false;
        }
        if (titleConfig != null ? !titleConfig.equals(that.titleConfig) : that.titleConfig!= null) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType!= null) {
            return false;
        }
        if (modalWidth != null ? !modalWidth.equals(that.modalWidth) : that.modalWidth!= null) {
            return false;
        }
        if (modalHeight != null ? !modalHeight.equals(that.modalHeight) : that.modalHeight!= null) {
            return false;
        }
        if (resizable != that.resizable) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (inline ? 1 : 0);
        result = 31 * result + (titleConfig != null ? titleConfig.hashCode() : 0);
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        result = 31 * result + (modalWidth != null ? modalWidth.hashCode() : 0);
        result = 31 * result + (modalHeight != null ? modalHeight.hashCode() : 0);
        result = 31 * result + (resizable ? 1 : 0);
        return result;
    }
}
