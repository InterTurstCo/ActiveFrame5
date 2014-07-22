package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.CreateConfig;
import ru.intertrust.cm.core.config.gui.form.widget.OperationConfig;
import ru.intertrust.cm.core.config.gui.form.widget.UpdateConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 17.07.2014
 *         Time: 13:02
 */
@Root(name = "on-delete")
public class OnDeleteConfig implements Dto {
    @Attribute(name = "do-delete", required = false)
    private boolean doDelete = true;

    @ElementListUnion({
            @ElementList(entry="create", type=CreateConfig.class, inline=true, required = false),
            @ElementList(entry="update", type=UpdateConfig.class, inline=true, required = false),
    })
    private List<OperationConfig> operationConfigs = new ArrayList<>();

    public boolean doDelete() {
        return doDelete;
    }

    public void setDoDelete(boolean doDelete) {
        this.doDelete = doDelete;
    }

    public List<OperationConfig> getOperationConfigs() {
        return operationConfigs;
    }

    public void setOperationConfigs(List<OperationConfig> operationConfigs) {
        this.operationConfigs = operationConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OnDeleteConfig that = (OnDeleteConfig) o;

        if (doDelete != that.doDelete) {
            return false;
        }
        if (operationConfigs != null ? !operationConfigs.equals(that.operationConfigs) : that.operationConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (doDelete ? 1 : 0);
        result = 31 * result + (operationConfigs != null ? operationConfigs.hashCode() : 0);
        return result;
    }
}
