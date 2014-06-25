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
@Root(name = "on-unlink")
public class OnUnlinkConfig implements Dto {

    @Attribute(name = "do-unlink", required = false, empty = "true")
    private boolean doUnlink;

    @ElementList(inline = true)
    private List<OperationConfig> activeFields = new ArrayList<OperationConfig>();

    public boolean getDoUnlink() {
        return doUnlink;
    }

    public void setDoUnlink(boolean doUnlink) {
        this.doUnlink = doUnlink;
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

        OnUnlinkConfig that = (OnUnlinkConfig) o;

        if (activeFields != null ? !activeFields.equals(that.activeFields) : that.activeFields != null) return false;
        if (doUnlink != that.doUnlink) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = doUnlink ? 1 : 0;
        result = 31 * result + (activeFields != null ? activeFields.hashCode() : 0);
        return result;
    }
}
