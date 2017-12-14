package ru.intertrust.cm.core.gui.impl.client.plugins.report;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
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

    @Override
    public PluginView createView() {
        final ReportPluginData initialData = getInitialData();
        return new ReportPluginView(this, initialData.getFormDisplayData());
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
        setDisplayActionToolBar(true);
        Application.getInstance().unlockScreen();
    }

    public String getReportName() {
        final ReportPluginData initialData = getInitialData();
        return initialData.getReportName();
    }

    public FormState getFormState() {
        final ReportPluginData initialData = getInitialData();
        return initialData.getFormDisplayData().getFormState();
    }

    public void updateFormState() {
        FormPanel formPanel = (FormPanel)getView().getViewWidget();
        for (BaseWidget widget : formPanel.getWidgets()) {
            WidgetState widgetState = widget.getCurrentState();

            if (widget instanceof LabelWidget) {
                continue;
            }
            String widgetId = widget.getDisplayConfig().getId();
            getFormState().setWidgetState(widgetId, widgetState);
        }
    }
}
