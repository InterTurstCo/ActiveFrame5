package ru.intertrust.cm.core.config.gui.form.widget.linkediting;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.2014
 *         Time: 6:40
 */
@Root(name = "created-object")
public class CreatedObjectConfig implements Dto {
    @Attribute(name = "text")
    @Localizable
    private String text;

    @Attribute(name = "domain-object-type")
    private String domainObjectType;



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CreatedObjectConfig that = (CreatedObjectConfig) o;

        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }
        if (text != null ? !text.equals(that.text) : that.text != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        return result;
    }
}
