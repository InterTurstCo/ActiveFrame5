package ru.intertrust.cm.core.gui.impl.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ActionConfig;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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
        SomeActivePluginData pluginData = new SomeActivePluginData();
        pluginData.setActionConfigs(getActions());
        pluginData.setForm(getGuiService().getForm(null));
        System.out.println("SomeActivePluginHandler initialized!");
        return pluginData;
    }

    public List<ActionConfig> getActions()  {
        ArrayList<ActionConfig> actions = new ArrayList<>();
        actions.add(new ActionConfig("Сохранить"));
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
}
