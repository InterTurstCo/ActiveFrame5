package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.RowConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.05.2015
 *         Time: 20:57
 */
@Root(name = "replace-trs")
public class ReplaceRowsConfig implements FormExtensionOperation {
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

        ReplaceRowsConfig that = (ReplaceRowsConfig) o;

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
