package ru.intertrust.cm.core.gui.impl.server.configextension;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravil on 16.05.2017.
 */
public abstract class ConfigExtensionsGuiActionBase extends ActionHandler<SimpleActionContext, SimpleActionData> {
    @Autowired
    protected ConfigurationControlService configurationControlService;

    protected List<DomainObject> getToolingDos(Id configurationDO){
        return crudService.findLinkedDomainObjects(configurationDO,"config_extension_tooling","configuration_extension");
    }

    protected List<DomainObject> getMultipleToolingDos(List<Id> configurationDO){
        List<DomainObject> objects = new ArrayList<>();
        for(Id i : configurationDO){
            objects.addAll(crudService.findLinkedDomainObjects(i,
                    "config_extension_tooling","configuration_extension"));
        }
        return objects;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }
}
