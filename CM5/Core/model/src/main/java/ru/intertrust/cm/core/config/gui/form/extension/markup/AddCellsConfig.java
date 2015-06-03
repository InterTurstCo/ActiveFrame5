package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.CellConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 11:03
 */
@Root(name = "add-tds")
public class AddCellsConfig implements FormExtensionOperation {
    @ElementListUnion({
            @ElementList(name = "after-td", type = AfterCellConfig.class, required = false, inline = true),
            @ElementList(name = "before-td", type = BeforeCellConfig.class, required = false, inline = true)
    })
    private List<IdentifiedFormExtensionOperation<CellConfig>> operations = new ArrayList<>();

    public List<IdentifiedFormExtensionOperation<CellConfig>> getOperations() {
        return operations;
    }

    public void setOperations(List<IdentifiedFormExtensionOperation<CellConfig>> operations) {
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

        AddCellsConfig that = (AddCellsConfig) o;

        if (operations != null ? !operations.equals(that.operations)
                : that.operations != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return operations != null ? operations.hashCode() : 0;
    }

}
