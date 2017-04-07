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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseAttributeConfig that = (BaseAttributeConfig) o;

        if (styleClass != null ? !styleClass.equals(that.styleClass) : that.styleClass != null) return false;
        if (rendered != null ? !rendered.equals(that.rendered) : that.rendered != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return styleClass != null ? styleClass.hashCode() : 0;
    }
}
