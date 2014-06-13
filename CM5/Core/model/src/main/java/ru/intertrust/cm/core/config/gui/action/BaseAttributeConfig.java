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

    @Attribute(name = "id", required = false)
    private String id;

    @Attribute(required = false)
    private String style;

    @Attribute(required = false)
    private String styleClass;

    @Attribute(required = false)
    private String addStyleClass;

    @Attribute(required = false)
    protected String rendered;

    public String getId() {
        return id;
    }

    public String getStyle() {
        return style;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public String getAddStyleClass() {
        return addStyleClass;
    }

    public String getRendered() {
        return rendered == null ?  Boolean.TRUE.toString() : rendered;
    }

    public void setRendered(final String rendered) {
        this.rendered = rendered;
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
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
        return id == null ? false : id.equals(other.id);
    }
}
