package ru.intertrust.cm.core.gui.impl.client.plugins.reportupload;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;

@ComponentName("report.package.import.plugin")
public class ReportPackageImportPlugin extends Plugin implements IsActive {

    @Override
    public PluginView createView() {
        return new ReportPackageImportPluginView(this);
    }

    @Override
    public Component createNew() {
        return new ReportPackageImportPlugin();
    }

    @Override
    public <E extends PluginState> E getPluginState() {
        return null;
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        // do nothing
    }

    @Override
    public void setInitialData(PluginData initialData) {
        super.setInitialData(initialData);
        setDisplayActionToolBar(true);
        Application.getInstance().unlockScreen();
    }

    public AttachmentItem getAttachmentItem() {
        ReportPackageImportPluginView view = (ReportPackageImportPluginView) getView();
        return view.getAttachmentItem();
    }

    public void clear() {
        ReportPackageImportPluginView view = (ReportPackageImportPluginView)getView();
        view.clear();
    }
}
