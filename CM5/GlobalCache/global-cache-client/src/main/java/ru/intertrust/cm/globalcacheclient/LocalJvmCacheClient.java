package ru.intertrust.cm.globalcacheclient;

import ru.intertrust.cm.core.dao.api.GlobalCacheClient;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2015
 *         Time: 14:17
 */
public abstract class LocalJvmCacheClient implements GlobalCacheClient {
    private boolean debugEnabled = false;

    @Override
    public boolean debugEnabled() {
        return debugEnabled;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    public abstract void init();
}
