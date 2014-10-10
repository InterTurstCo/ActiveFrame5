package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 11.04.2014 16:49.
 */
public class BaseAttributeConfig implements Dto {
    // @defaultUID
    private static final long serialVersionUID = 1L;

    @Attribute(required = false)
    private String styleClass;

    @Attribute(required = false)
    private String rendered;

    public String getStyleClass() {
        return styleClass;
    }

    public String getRendered() {
        return rendered == null ?  Boolean.TRUE.toString() : rendered;
    }

    public void setRendered(final String rendered) {
        this.rendered = rendered;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 17 + (styleClass == null ? 17 : styleClass.hashCode());
        result = result * 17 + (rendered == null ? 17 : rendered.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BaseAttributeConfig other = (BaseAttributeConfig) obj;
        if (styleClass == null ? other.styleClass != null : !styleClass.equals(other.styleClass)) {
            return false;
        }
        if (rendered == null ? other.rendered != null : !rendered.equals(other.rendered)) {
            return false;
        }
        return true;
    }
}
