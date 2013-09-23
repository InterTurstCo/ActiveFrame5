package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName("some.active.plugin")
public class SomeActivePluginHandler extends ActivePluginHandler {
    @Override
    public ActivePluginData initialize(Dto param) {
        SomeActivePluginConfig config = (SomeActivePluginConfig) param;
        String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        GuiService guiService = getGuiService();
        Form form = domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate) : guiService.getForm(config.getDomainObjectId());

        SomeActivePluginData pluginData = new SomeActivePluginData();
        pluginData.setActionConfigs(getActions());
        pluginData.setForm(form);//getId("country", 1L)));
        return pluginData;
    }

    public List<ActionConfig> getActions()  {
        ActionConfig saveAction = new ActionConfig("save.action", "save.action");
        saveAction.setText("Сохранить");
        ActionConfig createNewAction = new ActionConfig("create.new.object.action", "create.new.object.action");
        createNewAction.setText("Создать новый");
        ArrayList<ActionConfig> actions = new ArrayList<>();
        actions.add(saveAction);
        actions.add(createNewAction);
        return actions;
    }

    public ActivePluginData doSomethingVeryGood(Dto dto) {
        System.out.println("SomeActivePluginHandler executed doSomethingVeryGood()");
        return null;
    }

    private GuiService getGuiService() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            return (GuiService) ctx.lookup("java:app/web-app/GuiServiceImpl!ru.intertrust.cm.core.gui.api.server.GuiService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }

    private ConfigurationService getConfigurationService() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            return (ConfigurationService) ctx.lookup("java:app/web-app/ConfigurationServiceImpl!ru.intertrust.cm.core.business.api.ConfigurationService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }

    private Id getId(String doType, Long id) { // todo: rude hack. drop later
        Object obj = SpringApplicationContext.getContext().getBean("domainObjectTypeIdCache");
        try {
            Integer doTypeId = (Integer) obj.getClass().getMethod("getId", String.class).invoke(obj, doType);
            return new RdbmsId(doTypeId, id);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
