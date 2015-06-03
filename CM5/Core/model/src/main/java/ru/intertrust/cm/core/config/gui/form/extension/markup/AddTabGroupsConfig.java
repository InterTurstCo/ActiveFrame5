package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 12:01
 */
@Root(name = "add-tab-groups")
public class AddTabGroupsConfig implements FormExtensionOperation {
    @ElementListUnion({
            @ElementList(name = "after-tab-group", type = AfterTabGroupConfig.class, required = false, inline = true),
            @ElementList(name = "before-tab-group", type = BeforeTabGroupConfig.class, required = false, inline = true)
    })
    private List<IdentifiedFormExtensionOperation<TabGroupConfig>> operations = new ArrayList<>();

    public List<IdentifiedFormExtensionOperation<TabGroupConfig>> getOperations() {
        return operations;
    }

    public void setOperations(List<IdentifiedFormExtensionOperation<TabGroupConfig>> operations) {
        this.operations = operations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddTabGroupsConfig that = (AddTabGroupsConfig) o;

        if (operations != null ? !operations
                .equals(that.operations) : that.operations != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return operations != null ? operations.hashCode() : 0;
    }

}
