package ru.intertrust.cm.globalcacheclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.model.FatalException;

/**
 * @author Denis Mitavskiy
 *         Date: 21.08.2015
 *         Time: 18:15
 */
public class GlobalCacheSettings implements Dto {
    private static final long serialVersionUID = -1755477697564714229L;
    private static final Logger logger = LoggerFactory.getLogger(GlobalCacheSettings.class);
    public static final long DEFAULT_SIZE_LIMIT = 10L * 1024 * 1024;
    public static final int DEFAULT_WAIT_LOCK_MILLIES = 1;

    public enum Mode {
        NonBlocking {
            @Override
            public boolean isBlocking() {
                return false;
            }

            @Override
            public String toString() {
                return "non-blocking";
            }

            public String getBeanName() {
                return "globalCache";
            }
        },
        Blocking {
            @Override
            public boolean isBlocking() {
                return true;
            }

            @Override
            public String toString() {
                return "blocking";
            }

            public String getBeanName() {
                return "blockingGlobalCache";
            }
        },
        StrictlyBlocking {
            @Override
            public boolean isBlocking() {
                return true;
            }

            @Override
            public String toString() {
                return "strictly-blocking";
            }

            public String getBeanName() {
                return "strictlyBlockingGlobalCache";
            }
        },
        OptimisticStamped {
            @Override
            public boolean isBlocking() {
                return true;
            }

            @Override
            public String toString() {
                return "optimistic-stamped";
            }

            public String getBeanName() {
                return "optimisticStamped";
            }
        },
        Synchronized {
            @Override
            public boolean isBlocking() {
                return true;
            }

            @Override
            public String toString() {
                return "synchronized";
            }

            public String getBeanName() {
                return "synchronized";
            }
        },
        Stamped {
            @Override
            public boolean isBlocking() {
                return true;
            }

            @Override
            public String toString() {
                return "stamped";
            }

            public String getBeanName() {
                return "stamped";
            }
        };
        
        public abstract boolean isBlocking();

        public abstract String getBeanName();

        public static Mode getMode(String mode) {
            if (mode == null) {
                return Mode.Blocking;
            }
            
            for (Mode enumItem : Mode.values()) {
                if (enumItem.toString().equalsIgnoreCase(mode)) {
                    return enumItem;
                }
            }
            
            throw new FatalException("Not support global.cache.mode + " + mode + ". Please correct server.properties config file.");
        }
    }

    @Value("${global.cache.mode:blocking}")
    private volatile String mode;

    @Value("${global.cache.max.size:#{null}}")
    private volatile String sizeLimit;

    @Value("${global.cache.max.item.size:#{null}}")
    private volatile String sizeItemLimit;

    @Value("${global.cache.wait.lock.millies:#{1}}")
    private volatile String waitLockMilliesStr;

    @Value("${global.cache.cluster.synchronization.seconds:#{0}}")
    private volatile long clusterSynchronizationSeconds;

    @Value("${global.cache.cluster.mode:#{false}}")
    private volatile boolean inCluster;

    private volatile Long sizeLimitBytes = null;
    private volatile Long sizeItemLimitBytes = null;
    private volatile Integer waitLockMillies = null;

    public Mode getMode() {
        return Mode.getMode(mode);
    }

    public void setMode(Mode mode) {
        this.mode = mode.toString();
    }

    public boolean isInCluster() {
        return inCluster;
    }

    public long getClusterSynchronizationSeconds() {
        return clusterSynchronizationSeconds;
    }

    public long getClusterSynchronizationMillies() {
        return clusterSynchronizationSeconds * 1000;
    }

    public void setClusterSynchronizationSeconds(Long clusterSynchronizationSeconds) {
        this.clusterSynchronizationSeconds = clusterSynchronizationSeconds;
    }

    public void setSizeLimitBytes(long bytes) {
        sizeLimitBytes = bytes;
    }

    public Long getSizeLimitBytes() {
        if (sizeLimitBytes != null) {
            return sizeLimitBytes;
        }
        if (sizeLimit == null || sizeLimit.trim().isEmpty()) {
            sizeLimitBytes = DEFAULT_SIZE_LIMIT;
            return sizeLimitBytes;
        }

        sizeLimitBytes = sizeFromString("Global cache size", sizeLimit, DEFAULT_SIZE_LIMIT);
        return sizeLimitBytes;
    }

    public void setSizeItemLimitBytes(long bytes) {
        sizeItemLimitBytes = bytes;
    }

    public long getSizeItemLimitBytes() {
        if (sizeItemLimitBytes != null) {
            return sizeItemLimitBytes;
        }
        // По умолчанию значение одного объекта в кэшэ не может превышать 1% размера кэша
        long defaultValue = getSizeLimitBytes() / 100;
        if (sizeItemLimit == null || sizeItemLimit.trim().isEmpty()) {
            sizeItemLimitBytes = defaultValue;
            return sizeItemLimitBytes;
        }

        sizeItemLimitBytes = sizeFromString("Global cache item size", sizeItemLimit, defaultValue);
        return sizeItemLimitBytes;
    }

    private Long sizeFromString(String desc, String size, long defaultValue){
        Long result = null;
        final String limitStr = size.trim();
        char lastSymbol = limitStr.charAt(limitStr.length() - 1);
        String digits;
        if (Character.isDigit(lastSymbol)) {
            lastSymbol = 'M';
            digits = limitStr;
            logger.warn(desc + " size unit is not set, number will be treated as Megabytes");
        } else {
            digits = limitStr.substring(0, limitStr.length() - 1);
        }
        final long limitNumber;
        try {
            limitNumber = Long.parseLong(digits);
        } catch (NumberFormatException e) {
            result = defaultValue;
            logger.error(desc + " wrong size format: " + limitStr + "; set to default: " + defaultValue + " bytes");
            return result;
        }
        switch (lastSymbol) {
            case 'M':
            case 'm':
            case 'М':
            case 'м':
                result = limitNumber * 1024 * 1024;
                break;
            case 'G':
            case 'g':
            case 'Г':
            case 'г':
                result = limitNumber * 1024 * 1024 * 1024;
                break;
            default:
                result = defaultValue;
                logger.error(desc + " wrong size format: " + limitStr + "; set to default: " + defaultValue + " bytes");
        }
        return result;
    }

    public Integer getWaitLockMillies() {
        if (waitLockMillies != null) {
            return waitLockMillies;
        }
        if (waitLockMilliesStr == null) {
            waitLockMillies = DEFAULT_WAIT_LOCK_MILLIES;
            return waitLockMillies;
        }
        try {
            waitLockMillies = Integer.valueOf(waitLockMilliesStr);
        } catch (NumberFormatException e) {
            waitLockMillies = DEFAULT_WAIT_LOCK_MILLIES;
        }
        return waitLockMillies;
    }

    public void setWaitLockMillies(int waitLockMillies) {
        this.waitLockMillies = waitLockMillies;
    }
}
