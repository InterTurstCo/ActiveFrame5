package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName("form.plugin")
public class FormPluginHandler extends PluginHandler {
    @Autowired
    GuiService guiService;

    public PluginData initialize(Dto param) {
        FormPluginConfig config = (FormPluginConfig) param;
        String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        FormDisplayData form = domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate)
                : guiService.getForm(config.getDomainObjectId());

        FormPluginData pluginData = new FormPluginData();
        pluginData.setFormDisplayData(form);
        return pluginData;
    }
}
