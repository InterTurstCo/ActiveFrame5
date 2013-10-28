package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.ActionContextConfig;
import ru.intertrust.cm.core.config.model.DomainObjTypeConfig;
import ru.intertrust.cm.core.config.model.DomainObjectContextConfig;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Stateless
@Local(ActionService.class)
@Remote(ActionService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ActionServiceImpl implements ActionService, ActionService.Remote {

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    
    @Autowired
    private ProcessService processService;

    @Override
    public List<ActionContext> getActions(DomainObject domainObject) {
        List<ActionContext> list = new ArrayList<>();
        Collection <ActionContextConfig> actionContextConfigs = configurationExplorer.getConfigs(ActionContextConfig.class);
        for  (ActionContextConfig actionContextConfig:actionContextConfigs) {
            Collection <DomainObjectContextConfig> domainObjectContext = actionContextConfig.getDomainObjectContext();
            for  (DomainObjectContextConfig domainContextConfig:domainObjectContext) {
              List<DomainObjTypeConfig> domainObjectTypes = domainContextConfig.getDomainObjectType();
              for  (DomainObjTypeConfig domainObjectType:domainObjectTypes) {
                  if (domainObject.getClass().getName().equals(domainObjectType.getName())
                          && domainContextConfig.getStatus().contains(domainObject.getStatus())) {
                      List<ru.intertrust.cm.core.config.model.ActionConfig> actionConfigs = actionContextConfig.getAction();
                      for  (ru.intertrust.cm.core.config.model.ActionConfig actionConfig:actionConfigs) {
                          ActionConfig actConfig = configurationExplorer.getConfig(ActionConfig.class, actionConfig.getName());
                          list.add((ActionContext) actConfig.getActionSettingsConfig().getProcessAction());
                      }
                  }
              }
                
            }
            
        }
        List<DomainObject> tasks =  processService.getUserDomainObjectTasks(domainObject.getId());
        for  (DomainObject task:tasks) {
            ActionConfig actConfig = configurationExplorer.getConfig(ActionConfig.class, task.getTypeName());
            list.add((ActionContext) actConfig.getActionSettingsConfig().getProcessAction());
        }
        /*ActionConfig actionConfig= new ActionConfig();
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
        list.add(startProcess);*/
        return list;
    }


}
