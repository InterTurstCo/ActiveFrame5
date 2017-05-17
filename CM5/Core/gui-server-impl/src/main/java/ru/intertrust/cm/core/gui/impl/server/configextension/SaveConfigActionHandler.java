package ru.intertrust.cm.core.gui.impl.server.configextension;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.gui.impl.server.action.SaveActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravil on 17.05.2017.
 */
@ComponentName("save.config.action.handler")
public class SaveConfigActionHandler extends SaveActionHandler {
    private static final String FIELD_DRAFT = "draft_xml";
    private static final String FIELD_CONFIGEXTENSION = "configuration_extension";
    private static final String DO_NAME = "config_extension_tooling";
    private static final String WIDGET_NAME = "draftXMLArea";

    @Autowired
    ConfigurationControlService configurationControlService;

    @Autowired
    private ConfigurationExplorer configurationService;

    @Autowired
    CrudService crudService;

    @Override
    public SaveActionData executeAction(SaveActionContext context) {
        SaveActionData aData = new SaveActionData();
        if (context.getRootObjectId() != null) {
            aData =  super.executeAction(context);
            return aData;
        }

        List<DomainObject> toolingObjects = new ArrayList<>();
        DomainObject toolingObject = crudService.createDomainObject(DO_NAME);
        TextState tState = (TextState)context.getFormState().getFullWidgetsState().get(WIDGET_NAME);
        if(tState!=null){
            toolingObject.setString(FIELD_DRAFT,tState.getText());
        }
        toolingObjects.add(toolingObject);

        List<DomainObject> configObjects =  configurationControlService.saveDrafts(toolingObjects);
        if(configObjects.size()>0){
            FormPluginConfig config = new FormPluginConfig(configObjects.get(0).getReference(FIELD_CONFIGEXTENSION));
            config.setPluginState(context.getPluginState());
            config.setFormViewerConfig(context.getFormViewerConfig());
            FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
            BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                    BusinessUniverseConfig.NAME);
            aData.setFormPluginData(handler.initialize(config));
            aData.setDefaultFormEditingStyleConfig(businessUniverseConfig.getDefaultFormEditingStyleConfig());
        }


        return aData;
    }


}
