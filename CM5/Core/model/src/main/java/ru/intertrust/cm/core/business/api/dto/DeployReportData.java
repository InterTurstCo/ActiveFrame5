package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс описывает устанавливаемый отчет
 * @author larin
 *
 */
public class DeployReportData implements Dto{
    private List<DeployReportItem> items = new ArrayList<DeployReportItem>();
    public List<DeployReportItem> getItems() {
        return items;
    }
    public void setItems(List<DeployReportItem> items) {
        this.items = items;
    }
}
