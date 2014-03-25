package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 17:41
 */
public class GenerateReportActionData extends ActionData {

    private String reportName;
    Map<String, Value> params;

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Map<String, Value> getParams() {
        return params;
    }

    public void setParams(Map<String, Value> params) {
        this.params = params;
    }
}
