package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;

/**
 * Created by
 * Bondarchuk Yaroslav
 * 01.08.2014
 * 0:19
 */

public class UserSettingsUtil {
    public static ActionConfig createActionConfig() {
        final ActionConfig config = new ActionConfig();
        config.setDirtySensitivity(false);
        config.setImmediate(true);
        return config;
    }

}
