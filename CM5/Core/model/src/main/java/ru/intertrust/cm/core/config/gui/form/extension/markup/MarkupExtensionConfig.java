package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.05.2015
 *         Time: 20:50
 */
@Root(name = "markup-extension")
public class MarkupExtensionConfig implements Dto {
    @ElementListUnion({
            @ElementList(entry="add-tabs", type=AddTabsConfig.class, required = false, inline = true),
            @ElementList(entry="delete-tabs", type=DeleteTabsConfig.class, required = false, inline = true),
            @ElementList(entry="replace-tabs", type=ReplaceTabsConfig.class, required = false, inline = true),
            @ElementList(entry="add-tab-groups", type=AddTabGroupsConfig.class, required = false, inline = true),
            @ElementList(entry="delete-tab-groups", type=DeleteTabGroupsConfig.class, required = false, inline = true),
            @ElementList(entry="replace-tab-groups", type=ReplaceTabGroupsConfig.class, required = false, inline = true),
            @ElementList(entry="add-trs", type=AddRowsConfig.class, required = false, inline = true),
            @ElementList(entry="delete-trs", type=DeleteRowsConfig.class, required = false, inline = true),
            @ElementList(entry="replace-trs", type=ReplaceRowsConfig.class, required = false, inline = true),
            @ElementList(entry="add-tds", type=AddCellsConfig.class, required = false, inline = true),
            @ElementList(entry="delete-tds", type=DeleteCellsConfig.class, required = false, inline = true),
            @ElementList(entry="replace-tds", type=ReplaceCellsConfig.class, required = false, inline = true)
    })
    private List<FormExtensionOperation> operations = new ArrayList<FormExtensionOperation>();

    public List<FormExtensionOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<FormExtensionOperation> operations) {
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

        MarkupExtensionConfig that = (MarkupExtensionConfig) o;

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
