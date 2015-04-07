package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 06.04.2015
 */
@Element(name = "action-group-ref")
public class ActionGroupRefConfig extends BaseActionConfig {
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

    @Override
    public String toString() {
        return new StringBuilder(ActionGroupRefConfig.class.getSimpleName())
                .append(": nameRef=").append(nameRef)
                .toString();
    }
}
