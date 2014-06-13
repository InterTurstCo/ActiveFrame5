package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.util.ObjectCloner;

/**
 * @author Sergey.Okolot
 *         Created on 06.06.2014 12:00.
 */
public class PluginHelper {

    private PluginHelper() {}

    public static ActionConfig createActionConfig(final String name, final String component,
                                                  final String label, final String imageUrl) {
        final ActionConfig config = new ActionConfig(name, component);
        config.setText(label);
        config.setImageUrl(imageUrl);
        return config;
    }

    public static ActionConfig cloneConfig(final ActionConfig config) {
        final ObjectCloner cloner = new ObjectCloner();
        final ActionConfig result = cloner.cloneObject(config, ActionConfig.class);
        return result;
    }
}
