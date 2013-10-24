package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.config.model.gui.ActionSettingsConfig;
import ru.intertrust.cm.core.config.model.gui.StartProcessActionSettings;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionContext;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Local(ActionService.class)
@Remote(ActionService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ActionServiceImpl implements ActionService, ActionService.Remote {

    @Override
    public List<ActionContext> getActions(DomainObject domainObject) {
        List<ActionContext> list = new ArrayList<>();
        ActionConfig actionConfig= new ActionConfig();
        actionConfig.setName("start.process.action");
        actionConfig.setComponent("start.process.action");
        actionConfig.setText("Начать процесс");
        actionConfig.setImageUrl("sign.png");
        actionConfig.setShowText(false);
        ActionSettingsConfig actionSettingsConfig = new ActionSettingsConfig();
        StartProcessActionSettings startProcessSettings = new StartProcessActionSettings();
        startProcessSettings.setClassName("StartProccessAction");
        startProcessSettings.setProcessName("execution");
        actionSettingsConfig.setProcessAction(startProcessSettings);
        actionConfig.setActionSettingsConfig(actionSettingsConfig);
        StartProcessActionContext startProcess = new StartProcessActionContext();
        startProcess.setActionConfig(actionConfig);
        list.add(startProcess);
        return list;
    }

}
