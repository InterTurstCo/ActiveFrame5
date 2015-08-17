package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.04.2015
 */
@Root(name = "attachment-viewer")
public class AttachmentViewerConfig extends WidgetConfig implements Dto {
    private static final String COMPONENT_NAME = "attachment-viewer";

    @Attribute(name = "width", required = false)
    protected String width;

    @Attribute(name = "height", required = false)
    protected String height;

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
