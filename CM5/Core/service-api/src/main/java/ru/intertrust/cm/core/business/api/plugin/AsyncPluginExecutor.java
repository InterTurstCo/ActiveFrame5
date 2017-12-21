package ru.intertrust.cm.core.business.api.plugin;

import java.util.concurrent.Future;

public interface AsyncPluginExecutor {
    Future<Void> execute(String pluginId, String param);
}
