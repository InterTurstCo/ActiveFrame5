package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
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

    @Attribute(name = "do-link", required = false)
    private String doLink;

    @ElementList(inline = true)
    private List<OperationConfig> activeFields = new ArrayList<OperationConfig>();

    public String getDoLink() {
        return doLink;
    }

    public void setDoLink(String doLink) {
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
        if (doLink != null ? !doLink.equals(that.doLink) : that.doLink != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = doLink != null ? doLink.hashCode() : 0;
        result = 31 * result + (activeFields != null ? activeFields.hashCode() : 0);
        return result;
    }
}
