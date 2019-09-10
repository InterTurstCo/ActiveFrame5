package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.AttributeConfig;
import ru.intertrust.cm.core.config.gui.navigation.CustomPluginConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.SingleRowPluginData;
import ru.intertrust.cm.core.model.FatalException;

@ComponentName("single.row.plugin")
public class SingleRowPluginHandler extends PluginHandler{
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private CrudService crudService;

    @Override
    public PluginData initialize(Dto config) {
        CustomPluginConfig customPluginConfig = (CustomPluginConfig)config;
        String type = null;
        for (AttributeConfig attributeConfig : customPluginConfig.getAttributeConfigList()) {
            if (attributeConfig.getName().equalsIgnoreCase("type")) {
                type = attributeConfig.getValue(); 
            }
        }
        
        if (type == null) {
            throw new FatalException("Type name not configured");
        }

        SingleRowPluginData pluginData = new SingleRowPluginData();
        
        final FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        final FormPluginConfig formPluginConfig = new FormPluginConfig();
        
        List<DomainObject> list = crudService.findAll(type);
        if (list.size() > 0) {
            formPluginConfig.setDomainObjectId(list.get(0).getId());
        }else {
            formPluginConfig.setDomainObjectTypeToCreate(type);
        }
        
        final FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        
        pluginData.setFormPluginData(formPluginData);
        return pluginData;
    }
}
