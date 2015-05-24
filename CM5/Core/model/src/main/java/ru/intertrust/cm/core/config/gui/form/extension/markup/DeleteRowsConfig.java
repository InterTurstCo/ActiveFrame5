package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.RowConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 12:21
 */
@Root(name = "delete-trs")
public class DeleteRowsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "tr")
    private List<RowConfig> rowConfigs = new ArrayList<RowConfig>();

    public List<RowConfig> getRowConfigs() {
        return rowConfigs;
    }

    public void setRowConfigs(List<RowConfig> rowConfigs) {
        this.rowConfigs = rowConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeleteRowsConfig that = (DeleteRowsConfig) o;

        if (rowConfigs != null ? !rowConfigs.equals(that.rowConfigs) : that.rowConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return rowConfigs != null ? rowConfigs.hashCode() : 0;
    }

}
