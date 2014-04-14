package ru.intertrust.cm.core.gui.impl.client.plugins.reportupload;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 14.04.14
 *         Time: 12:39
 */
@ComponentName("report.upload.plugin")
public class ReportUploadPlugin extends Plugin implements IsActive {

    @Override
    public PluginView createView() {
        return new ReportUploadPluginView(this);
    }

    @Override
    public Component createNew() {
        return new ReportUploadPlugin();
    }

    @Override
    public <E extends PluginState> E getPluginState() {
        return null; //TODO: implement
    }

    @Override
    public void setPluginState(PluginState pluginState) {
        //TODO: implement
    }

    @Override
    public void setInitialData(PluginData initialData) {
        super.setInitialData(initialData);
        setDisplayActionToolBar(true);
        Application.getInstance().hideLoadingIndicator();
    }

    public List<AttachmentItem> getAttachmentItems() {
        ReportUploadPluginView uploadView = (ReportUploadPluginView)getView();
        AttachmentBoxState state = (AttachmentBoxState)uploadView.getAttachmentBox().getCurrentState();

        return state.getAttachments();
    }

}
