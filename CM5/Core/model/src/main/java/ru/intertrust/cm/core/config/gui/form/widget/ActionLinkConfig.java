package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by andrey on 28.03.14.
 */
@Root(name = "action-link")
public class ActionLinkConfig implements Dto {

    @Attribute(name = "text", required = true)
    private String text;
    @Attribute(name = "action-name", required = true)
    private String actionName;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionLinkConfig that = (ActionLinkConfig) o;

        if (!actionName.equals(that.actionName)) return false;
        if (!text.equals(that.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + actionName.hashCode();
        return result;
    }
}
