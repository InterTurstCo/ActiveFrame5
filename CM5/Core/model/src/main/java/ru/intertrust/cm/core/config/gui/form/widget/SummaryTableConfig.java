package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "summary-table")
public class SummaryTableConfig implements Dto {

    @ElementList(inline = true)
    private List<SummaryTableColumnConfig> summaryTableColumnConfigList = new ArrayList<SummaryTableColumnConfig>();

    @Deprecated
    public List<SummaryTableColumnConfig> getSummaryTableColumnConfig() {
        return summaryTableColumnConfigList;
    }

    public List<SummaryTableColumnConfig> getSummaryTableColumnConfigList() {
        return summaryTableColumnConfigList;
    }

    public void setSummaryTableColumnConfigList(List<SummaryTableColumnConfig> summaryTableColumnConfigList) {
        this.summaryTableColumnConfigList = summaryTableColumnConfigList;
    }

    @Deprecated
    public void setSummaryTableColumnConfig(List<SummaryTableColumnConfig> summaryTableColumnConfigList) {
        this.summaryTableColumnConfigList = summaryTableColumnConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SummaryTableConfig that = (SummaryTableConfig) o;

        if (summaryTableColumnConfigList != null ? !summaryTableColumnConfigList.equals(that.summaryTableColumnConfigList) : that.summaryTableColumnConfigList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return summaryTableColumnConfigList != null ? summaryTableColumnConfigList.hashCode() : 0;
    }
}
