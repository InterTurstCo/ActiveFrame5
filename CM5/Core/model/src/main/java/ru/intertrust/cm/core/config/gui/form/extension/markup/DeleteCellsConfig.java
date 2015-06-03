package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.CellConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 12:13
 */
@Root(name = "delete-tds")
public class DeleteCellsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "td")
    private List<CellConfig> cellConfigs = new ArrayList<CellConfig>();

    public List<CellConfig> getCellConfigs() {
        return cellConfigs;
    }

    public void setCellConfigs(List<CellConfig> cellConfigs) {
        this.cellConfigs = cellConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeleteCellsConfig that = (DeleteCellsConfig) o;

        if (cellConfigs != null ? !cellConfigs.equals(that.cellConfigs) : that.cellConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return cellConfigs != null ? cellConfigs.hashCode() : 0;
    }
}
