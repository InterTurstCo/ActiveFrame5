package ru.intertrust.cm.core.gui.impl.server;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.config.model.gui.ActionSettingsConfig;
import ru.intertrust.cm.core.config.model.gui.StartProcessActionSettings;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionContext;

public class ActionServiceImpl implements ActionService {

    @Override
    public List<ActionContext> getActions(DomainObject domainObject) {
        List<ActionContext> list = new ArrayList();
        ActionConfig actionConfig= new ActionConfig();
        actionConfig.setName("sign.action");
        actionConfig.setComponent("specific.sign.action");
        actionConfig.setText("Подписать");
        actionConfig.setImageUrl("sign.png");
        actionConfig.setShowText(false);
        ActionSettingsConfig actionSettingsConfig = new ActionSettingsConfig();
        StartProcessActionSettings startProcessSettings = new StartProcessActionSettings();
        startProcessSettings.setClassName("StartProccessAction");
        startProcessSettings.setProcessName("execution");
        actionSettingsConfig.setProcessAction(startProcessSettings);
        actionConfig.setActionSettingsConfig(actionSettingsConfig);
        StartProcessActionContext startProcess = new StartProcessActionContext();
        list.add(startProcess);
        return list;
    }

}
