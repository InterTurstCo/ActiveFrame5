package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 11:50
 */
@Root(name = "add-tabs")
public class AddTabsConfig implements FormExtensionOperation {
    @ElementListUnion({
            @ElementList(name = "after-tab", type = AfterTabConfig.class, required = false, inline = true),
            @ElementList(name = "before-tab", type = BeforeTabConfig.class, required = false, inline = true)
    })
    private List<IdentifiedFormExtensionOperation<TabConfig>> operations =  new ArrayList<>();

    public List<IdentifiedFormExtensionOperation<TabConfig>> getOperations() {
        return operations;
    }

    public void setOperations(List<IdentifiedFormExtensionOperation<TabConfig>> operations) {
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

        AddTabsConfig that = (AddTabsConfig) o;

        if (operations != null ? !operations
                .equals(that.operations) : that.operations != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return operations != null ? operations.hashCode() : 0;
    }

}
