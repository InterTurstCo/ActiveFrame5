package ru.intertrust.cm.globalcacheclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Denis Mitavskiy
 *         Date: 21.08.2015
 *         Time: 18:15
 */
public class GlobalCacheSettings {
    private static final Logger logger = LoggerFactory.getLogger(GlobalCacheSettings.class);
    public static final long DEFAULT_SIZE_LIMIT = 10L * 1024 * 1024;

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
    private Boolean enabled;

    @Value("${global.cache.mode}")
    private String defaultMode;

    @Value("${global.cache.debug.enabled}")
    private Boolean debugEnabled;

    @Value("${global.cache.max.size}")
    private String sizeLimit;

    private Long sizeLimitBytes = -1L;

    public boolean isEnabled() {
        return enabled == Boolean.TRUE;
    }

    public boolean isDebugEnabled() {
        return debugEnabled == Boolean.TRUE;
    }

    public Mode getMode() {
        return defaultMode != null && defaultMode.equalsIgnoreCase("non-blocking") ? Mode.NonBlocking : Mode.Blocking;
    }

    public Long getSizeLimitBytes() {
        if (sizeLimitBytes != -1L) {
            return sizeLimitBytes;
        }
        if (sizeLimit == null || sizeLimit.trim().isEmpty()) {
            sizeLimitBytes = null;
            return sizeLimitBytes;
        }
        final String limitStr = sizeLimit.trim();
        char lastSymbol = limitStr.charAt(limitStr.length() - 1);
        String digits;
        if (Character.isDigit(lastSymbol)) {
            lastSymbol = 'M';
            digits = limitStr;
            logger.warn("Global cache size unit is not set, number will be treated as Megabytes");
        } else {
            digits = limitStr.substring(0, limitStr.length() - 1);
        }
        final long limitNumber;
        try {
            limitNumber = Long.parseLong(digits);
        } catch (NumberFormatException e) {
            sizeLimitBytes = DEFAULT_SIZE_LIMIT;
            logger.error("Wrong global cache size format: " + limitStr + "; set to default: 10 Megabytes");
            return sizeLimitBytes;
        }
        switch (lastSymbol) {
            case 'M':
            case 'm':
            case 'М':
            case 'м':
                sizeLimitBytes = limitNumber * 1024 * 1024;
                break;
            case 'G':
            case 'g':
            case 'Г':
            case 'г':
                sizeLimitBytes = limitNumber * 1024 * 1024 * 1024;
                break;
            default:
                sizeLimitBytes = DEFAULT_SIZE_LIMIT;
                logger.error("Wrong global cache size format: " + limitStr + "; set to default: 10 Megabytes");
        }
        return sizeLimitBytes;
    }
}
