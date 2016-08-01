package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertyResolver;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.plugin.ExecutePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginInfoData;
import ru.intertrust.cm.core.gui.model.plugin.UploadData;

@ComponentName("plugin.manager.plugin")
public class PluginManagerPluginHandler extends PluginHandler {
    @Autowired
    private PropertyResolver propertyResolver;

    @Autowired
    private ApplicationContext context;    
    
    public PluginManagerPluginHandler(){
        super();
    }

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";

    public PluginInfoData refreshPlugins(Dto request) {
        PluginInfoData data = new PluginInfoData();
        data.getPluginInfos().addAll(context.getBean(PluginService.class).getPlugins().values());
        return data;
    }

    public void deployPlugins(Dto request) {
        UploadData uploadData = (UploadData) request;
        List<AttachmentItem> attachmentItems = uploadData.getAttachmentItems();
        String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
        for (AttachmentItem attachmentItem : attachmentItems) {
            File file = new File(pathForTempFilesStore, attachmentItem.getTemporaryName());
            if (file.getName().toLowerCase().endsWith(".jar")) {
                //Установка плагина
                context.getBean(PluginService.class).deployPluginPackage(file.getPath());
            }
        }
    }

    public StringValue executePlugin(Dto request) {
        ExecutePluginData executePluginData = (ExecutePluginData) request;
        return new StringValue(context.getBean(PluginService.class).executePlugin(executePluginData.getPluginId(), executePluginData.getParameter()));
    }

}
