package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 17:47
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "summary-table")
public class SummaryTableConfig implements Dto {
    @Attribute(name = "form-name")
    private String formName;

    @ElementList(inline = true)
    private List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public List<ColumnConfig> getColumnConfigs() {
        return columnConfigs;
    }

    public void setColumnConfigs(List<ColumnConfig> columnConfigs) {
        this.columnConfigs = columnConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SummaryTableConfig that = (SummaryTableConfig) o;

        if (columnConfigs != null ? !columnConfigs.equals(that.columnConfigs) : that.columnConfigs != null) {
            return false;
        }
        if (formName != null ? !formName.equals(that.formName) : that.formName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = formName != null ? formName.hashCode() : 0;
        result = 31 * result + (columnConfigs != null ? columnConfigs.hashCode() : 0);
        return result;
    }
}
