package ru.intertrust.cm.globalcacheclient;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Denis Mitavskiy
 *         Date: 21.08.2015
 *         Time: 18:15
 */
public class GlobalCacheSettings {
    public enum Mode {
        NonBlocking {
            @Override
            boolean isBlocking() {
                return false;
            }
        },
        Blocking {
            @Override
            boolean isBlocking() {
                return true;
            }
        };

        abstract boolean isBlocking();
    }

    @Value("${global.cache.enabled}")
    private Boolean enabledByDefault;

    @Value("${global.cache.mode}")
    private String defaultMode;

    public boolean isEnabled() {
        return enabledByDefault == Boolean.TRUE;
    }

    public Mode getMode() {
        return defaultMode != null && defaultMode.equalsIgnoreCase("blocking") ? Mode.Blocking : Mode.NonBlocking;
    }
}
