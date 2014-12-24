package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * Created by andrey on 08.12.14.
 */
public class SummaryTableActionsColumnConfig implements Dto {
    @ElementList(name = "action", required = false, inline = true, entry = "action")
    private List<SummaryTableActionColumnConfig> summaryTableActionColumnConfig;

    public List<SummaryTableActionColumnConfig> getSummaryTableActionColumnConfig() {
        return summaryTableActionColumnConfig;
    }

    public void setSummaryTableActionColumnConfig(List<SummaryTableActionColumnConfig> summaryTableActionColumnConfig) {
        this.summaryTableActionColumnConfig = summaryTableActionColumnConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SummaryTableActionsColumnConfig that = (SummaryTableActionsColumnConfig) o;

        if (summaryTableActionColumnConfig != null ? !summaryTableActionColumnConfig.equals(that.summaryTableActionColumnConfig) : that.summaryTableActionColumnConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return summaryTableActionColumnConfig != null ? summaryTableActionColumnConfig.hashCode() : 0;
    }
}
