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
public class OnUnlinkConfig implements Dto {

    @Attribute(name = "do-unlink", required = false, empty = "true")
    private boolean doUnlink;

    @ElementList(inline = true)
    private List<OperationConfig> operationConfigs = new ArrayList<OperationConfig>();

    public boolean doUnlink() {
        return doUnlink;
    }

    public void setDoUnlink(boolean doUnlink) {
        this.doUnlink = doUnlink;
    }

    public List<OperationConfig> getOperationConfigs() {
        return operationConfigs;
    }

    public void setOperationConfigs(List<OperationConfig> operationConfigs) {
        this.operationConfigs = operationConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnUnlinkConfig that = (OnUnlinkConfig) o;

        if (operationConfigs != null ? !operationConfigs.equals(that.operationConfigs) : that.operationConfigs != null) return false;
        if (doUnlink != that.doUnlink) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = doUnlink ? 1 : 0;
        result = 31 * result + (operationConfigs != null ? operationConfigs.hashCode() : 0);
        return result;
    }
}
