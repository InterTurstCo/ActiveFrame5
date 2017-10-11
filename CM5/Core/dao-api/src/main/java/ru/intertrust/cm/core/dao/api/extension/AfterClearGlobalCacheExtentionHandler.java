package ru.intertrust.cm.core.dao.api.extension;

public interface AfterClearGlobalCacheExtentionHandler extends ExtensionPointHandler{
    void onClearGlobalCache();
}
