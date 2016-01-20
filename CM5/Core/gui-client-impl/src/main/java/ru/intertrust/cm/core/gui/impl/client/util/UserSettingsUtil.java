package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.user.client.Timer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.History;
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
    private static InitialNavigationLinkAction initialNavigationLinkAction = null;
    private static NavigationPanelStateAction navigationPanelStateAction = null;

    private static final Timer initialNavigationLinkActionTimer = new Timer() {
        @Override
        public void run() {
            if (initialNavigationLinkAction != null) {
                initialNavigationLinkAction.perform();
            }
            this.cancel();

        }
    };

    private static final Timer navigationPanelStateActionTimer = new Timer() {
        @Override
        public void run() {
            if (navigationPanelStateAction != null) {
                navigationPanelStateAction.perform();
            }
            this.cancel();

        }
    };

    public static ActionConfig createActionConfig() {
        final ActionConfig config = new ActionConfig();
        config.setDirtySensitivity(false);
        config.setImmediate(true);
        return config;
    }

    public static void storeCurrentNavigationLink() {
        // wait for DELAY_FOR_CURRENT_LINK_STORING and then execute the action
        initialNavigationLinkActionTimer.cancel();

        InitialNavigationLinkActionContext context = new InitialNavigationLinkActionContext(UserSettingsUtil.createActionConfig());
        context.setInitialNavigationLink(History.getToken());
        context.setApplication(History.getApplication());
        InitialNavigationLinkAction action = ComponentRegistry.instance.get(InitialNavigationLinkActionContext.COMPONENT_NAME);
        action.setInitialContext(context);

        initialNavigationLinkAction = action;
        initialNavigationLinkActionTimer.schedule(DELAY_FOR_CURRENT_LINK_STORING);

    }

    public static void storeNavigationPanelState(final boolean pinned) {
        navigationPanelStateActionTimer.cancel();

        NavigationPanelStateActionContext context = new NavigationPanelStateActionContext(UserSettingsUtil.createActionConfig());
        context.setPinned(pinned);
        NavigationPanelStateAction action = ComponentRegistry.instance.get(NavigationPanelStateActionContext.COMPONENT_NAME);
        action.setInitialContext(context);

        navigationPanelStateAction = action;
        navigationPanelStateActionTimer.schedule(DELAY_FOR_CURRENT_LINK_STORING);
    }

}
