package ru.intertrust.cm.deployment.tool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * Created by alexander on 01.08.16.
 */
public final class ResourceUtil {

    private static Logger logger = LoggerFactory.getLogger(ResourceUtil.class);

    private ResourceUtil() {
        // nothing
    }

    public static String getServerPropValue(String propLine) {
        if (!isEmpty(propLine)) {
            int idx = propLine.indexOf("=");
            if (idx != -1) {
                String propKey = propLine.substring(idx + 1);
                logger.info("Server property key found - {}", propKey);
                return propKey;
            } else {
                logger.error("Server property value not valid, symbol '=' is absent - {}", propLine);
            }
        } else {
            logger.error("Server property must be not null", new IllegalArgumentException());
        }
        return null;
    }
}
