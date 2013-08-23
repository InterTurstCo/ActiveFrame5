package ru.intertrust.cm.core.gui.impl.server;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.SomeActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.SomePluginHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.lang.reflect.InvocationTargetException;

/**
 * Базовая реализация сервиса GUI
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:14
 */
@Stateless
@DeclareRoles("cm_user")
@RolesAllowed("cm_user")
@Local(GuiService.class)
@Remote(GuiService.Remote.class)
public class GuiServiceImpl implements GuiService, GuiService.Remote {
    @Resource
    private javax.ejb.SessionContext sessionContext;

    @Override
    public NavigationConfig getNavigationConfiguration() {
        return null;
    }

    @Override
    public PluginData executeCommand(Command command) {
        // todo: сделать по-человечески через Reflection или Spring Beans
        PluginHandler pluginHandler = null;
        switch (command.getComponentName()) {
            case "some.plugin":
                pluginHandler = new SomePluginHandler();
                break;
            case "some.active.plugin":
                pluginHandler = new SomeActivePluginHandler();
                break;
        }
        if (pluginHandler == null) {
            return null;
        }

        try {
            return (PluginData) PluginHandler.class.getMethod(command.getName(), Dto.class)
                    .invoke(pluginHandler, command.getParameter());
        } catch (NoSuchMethodException e) {
            throw new GuiException("No command + " + command.getName() + " implemented");
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new GuiException("Command can't be executed: " + command.getName());
        }
    }
}
