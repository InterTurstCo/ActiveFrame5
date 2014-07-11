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
public class OnLinkConfig implements Dto {

    @Attribute(name = "do-link", required = false, empty = "true")
    private boolean doLink;

    @ElementList(inline = true)
    private List<OperationConfig> operationConfigs = new ArrayList<OperationConfig>();

    public boolean doLink() {
        return doLink;
    }

    public void setDoLink(boolean doLink) {
        this.doLink = doLink;
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

        OnLinkConfig that = (OnLinkConfig) o;

        if (operationConfigs != null ? !operationConfigs.equals(that.operationConfigs) : that.operationConfigs != null) return false;
        if (doLink != that.doLink) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = doLink ? 1 : 0;
        result = 31 * result + (operationConfigs != null ? operationConfigs.hashCode() : 0);
        return result;
    }
}
