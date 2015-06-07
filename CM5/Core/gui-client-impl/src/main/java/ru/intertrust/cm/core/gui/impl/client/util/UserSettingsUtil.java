package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.system.InitialNavigationLinkAction;
import ru.intertrust.cm.core.gui.model.action.system.InitialNavigationLinkContext;

/**
 * Created by
 * Bondarchuk Yaroslav
 * 01.08.2014
 * 0:19
 */

public class UserSettingsUtil {
    private static final int DELAY_FOR_CURRENT_LINK_STORING = 5000;
    public static ActionConfig createActionConfig() {
        final ActionConfig config = new ActionConfig();
        config.setDirtySensitivity(false);
        config.setImmediate(true);
        return config;
    }

    public static void storeCurrentNavigationLink() {
        final Timer timer = new Timer() {
            @Override
            public void run() {
                InitialNavigationLinkContext context = new InitialNavigationLinkContext(UserSettingsUtil.createActionConfig());
                context.setInitialNavigationLink(History.getToken());
                InitialNavigationLinkAction action = ComponentRegistry.instance.get(InitialNavigationLinkContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
                this.cancel();

            }
        };
        timer.schedule(DELAY_FOR_CURRENT_LINK_STORING);

    }

}
