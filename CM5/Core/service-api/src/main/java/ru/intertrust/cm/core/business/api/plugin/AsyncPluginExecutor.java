package ru.intertrust.cm.core.business.api.plugin;

public interface AsyncPluginExecutor {
    void execute(String pluginId, String param);
}
