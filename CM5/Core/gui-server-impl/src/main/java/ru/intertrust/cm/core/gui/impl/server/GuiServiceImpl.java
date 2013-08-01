package ru.intertrust.cm.core.gui.impl.server;

import ru.intertrust.cm.core.config.model.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;

import javax.annotation.Resource;

/**
 * Базовая реализация сервиса GUI
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:14
 */
public class GuiServiceImpl implements GuiService {
    @Resource
    private javax.ejb.SessionContext sessionContext;
    /**
     * Атрибут, в котором хранятся данные авторизованного пользователя
     */
    public static final String USER_CREDENTIALS_SESSION_ATTRIBUTE = "_USER_CREDENTIALS";

    @Override
    public NavigationConfig getNavigationConfiguration() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
