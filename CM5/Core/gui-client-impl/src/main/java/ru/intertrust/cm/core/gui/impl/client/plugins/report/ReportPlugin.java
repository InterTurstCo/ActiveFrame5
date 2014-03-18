package ru.intertrust.cm.core.gui.impl.client.plugins.report;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;
import ru.intertrust.cm.core.gui.model.plugin.ReportPluginData;
import ru.intertrust.cm.core.gui.model.plugin.ReportPluginState;

/**
 * Плагин отображения отчётов
 *
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:27
 */
@ComponentName("report.plugin")
public class ReportPlugin extends Plugin implements IsActive {

    private String reportName;

    @Override
    public PluginView createView() {
        return new ReportPluginView(this);
    }

    @Override
    public Component createNew() {
        return new ReportPlugin();
    }

    @Override
    public ReportPluginState getPluginState() {
        ReportPluginData data = getInitialData();
        return (ReportPluginState) data.getPluginState().createClone();
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        ReportPluginData data = getInitialData();
        data.setPluginState(pluginState);
    }

    @Override
    public void setInitialData(PluginData initialData) {
        super.setInitialData(initialData);

        ReportPluginData reportPluginData = (ReportPluginData) initialData;
        reportName = reportPluginData.getReportName();
        setDisplayActionToolBar(true);
    }

    public String getReportName() {
        return reportName;
    }

}
