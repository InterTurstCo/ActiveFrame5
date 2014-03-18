package ru.intertrust.cm.core.gui.impl.client.plugins.report;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:53
 */
public class ReportPluginView extends PluginView {

    protected ReportPluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    public IsWidget getViewWidget() {
        Panel reportPanel = new SimplePanel();
        String reportName = ((ReportPlugin)plugin).getReportName();
        reportPanel.add(new Label("Report name: " + reportName));
        //TODO: [report-plugin] implement report view panel
        return reportPanel;
    }
}
