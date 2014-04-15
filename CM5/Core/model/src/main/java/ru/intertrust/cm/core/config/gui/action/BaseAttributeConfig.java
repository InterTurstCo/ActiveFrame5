package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;

/**
 * @author Sergey.Okolot
 *         Created on 11.04.2014 16:49.
 */
public class BaseAttributeConfig {

    @Attribute(name = "id", required = false)
    private String id;

    @Attribute(required = false)
    private String style;

    @Attribute(required = false)
    private String styleClass;

    @Attribute(required = false)
    private String addStyleClass;

    @Attribute(required = false)
    private boolean rendered;

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

    public boolean isRendered() {
        return rendered;
    }
}
