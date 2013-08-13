package ru.intertrust.cm.core.gui.impl.server;

import ru.intertrust.cm.core.config.model.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

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
}
