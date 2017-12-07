package ru.intertrust.cm.core.business.api.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.model.GwtIncompatible;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 04.12.2017
 *         Time: 18:47
 */
public class Case extends GwtCase {
    @GwtIncompatible
    private static final Logger logger = LoggerFactory.getLogger(Case.class);
    public static final int DEFAULT_MAX_SIZE = 512 * 1024;
    private static Map<String, String> map = new HashMap<>(); // also GWT-hack
    private static int maxSize = DEFAULT_MAX_SIZE;
    private static Case caseHelper;

    static {
        caseHelper = new Case();
        caseHelper.init(); // hack for GWT not supporting System properties
    }

    @GwtIncompatible
    protected void init() {
        final String propertiesLocation = System.getProperty("server.properties.location");
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream(new File(propertiesLocation, "server.properties")));
            maxSize = Integer.parseInt(properties.getProperty("max.lower.case.string.pool.size", String.valueOf(DEFAULT_MAX_SIZE)));
        } catch (Throwable e) {
            caseHelper.logError("Error loading server.properties", e);
        }
        map = new ConcurrentHashMap<>(maxSize, 0.75f, 128);
    }

    public static String toLower(String str) {
        final String cached = map.get(str);
        if (cached != null) {
            return cached;
        }
        final String result = str.toLowerCase();

        map.put(str, result);
        int size = map.size();
        if (size > maxSize) {
            caseHelper.logWarn("Max size of lower-case pool is exceeded: " + size + ". Removing entries");
            while (size > maxSize) {
                int counter = 0;
                for (String key : map.keySet()) {
                    map.remove(key);
                    if (++counter > 100) {
                        break;
                    }
                }
                size = map.size();
            }
            caseHelper.logWarn("Reduced lower-cased pool size to: " + size);
        }
        return result;
    }

    @GwtIncompatible
    protected void logError(String string, Throwable e) {
        logger.error(string, e);
    }

    @GwtIncompatible
    protected void logWarn(String string) {
        logger.warn(string);
    }

    @GwtIncompatible
    protected void logInfo(String string) {
        logger.info(string);
    }
}
