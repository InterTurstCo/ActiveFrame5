package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.config.gui.ActionConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 31.01.14
 *         Time: 13:15
 */
public class ActionConfigBuilder {
   public static ActionConfig createActionConfig(final String name, final String component,
                                                   final String label, final String imageUrl) {
        final ActionConfig config = new ActionConfig(name, component);
        config.setText(label);
        config.setImageUrl(imageUrl);
        return config;
    }

}
