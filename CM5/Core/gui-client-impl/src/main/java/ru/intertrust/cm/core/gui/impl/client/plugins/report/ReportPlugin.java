package ru.intertrust.cm.core.gui.impl.client.plugins.report;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.GenerateReportEvent;
import ru.intertrust.cm.core.gui.impl.client.event.GenerateReportEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
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

    private FormDisplayData formDisplayData;

    public ReportPlugin() {
        eventBus = GWT.create(SimpleEventBus.class);
    }

    private EventBus eventBus;

    @Override
    public PluginView createView() {
        return new ReportPluginView(this, formDisplayData);
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
        final String reportName = reportPluginData.getReportName();
        formDisplayData = reportPluginData.getFormDisplayData();
        setDisplayActionToolBar(true);

        eventBus.addHandler(GenerateReportEvent.TYPE, new GenerateReportEventHandler(){
            @Override
            public void generateReport() {
                String query = com.google.gwt.core.client.GWT.getHostPageBaseURL() + "generate-report?report_name=" + reportName;
                Window.open(query, reportName, "");
            }
        });
    }

    @Override
    public EventBus getLocalEventBus() {
        return eventBus;
    }

}
