package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.gui.api.client.History;
import com.google.gwt.user.client.Timer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.system.InitialNavigationLinkAction;
import ru.intertrust.cm.core.gui.impl.client.action.system.NavigationPanelStateAction;
import ru.intertrust.cm.core.gui.model.action.system.InitialNavigationLinkActionContext;
import ru.intertrust.cm.core.gui.model.action.system.NavigationPanelStateActionContext;

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
                InitialNavigationLinkActionContext context = new InitialNavigationLinkActionContext(UserSettingsUtil.createActionConfig());
                context.setInitialNavigationLink(History.getToken());
                context.setApplication(History.getApplication());
                InitialNavigationLinkAction action = ComponentRegistry.instance.get(InitialNavigationLinkActionContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
                this.cancel();

            }
        };
        timer.schedule(DELAY_FOR_CURRENT_LINK_STORING);

    }

    public static void storeNavigationPanelState(final boolean pinned) {
        final Timer timer = new Timer() {
            @Override
            public void run() {
                NavigationPanelStateActionContext context = new NavigationPanelStateActionContext(UserSettingsUtil.createActionConfig());
                context.setPinned(pinned);
                NavigationPanelStateAction action = ComponentRegistry.instance.get(NavigationPanelStateActionContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
                this.cancel();

            }
        };
        timer.schedule(DELAY_FOR_CURRENT_LINK_STORING);

    }

}
