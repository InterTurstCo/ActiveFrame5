package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbilyi on 18.06.2014.
 */
@Root(name = "on-link")
public class OnLinkConfig implements Dto {

    @Attribute(name = "do-link", required = false, empty = "true")
    private boolean doLink;

    @ElementList(inline = true)
    private List<OperationConfig> activeFields = new ArrayList<OperationConfig>();

    public boolean getDoLink() {
        return doLink;
    }

    public void setDoLink(boolean doLink) {
        this.doLink = doLink;
    }

    public List<OperationConfig> getActiveFields() {
        return activeFields;
    }

    public void setActiveFields(List<OperationConfig> activeFields) {
        this.activeFields = activeFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnLinkConfig that = (OnLinkConfig) o;

        if (activeFields != null ? !activeFields.equals(that.activeFields) : that.activeFields != null) return false;
        if (doLink != that.doLink) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = doLink ? 1 : 0;
        result = 31 * result + (activeFields != null ? activeFields.hashCode() : 0);
        return result;
    }
}
