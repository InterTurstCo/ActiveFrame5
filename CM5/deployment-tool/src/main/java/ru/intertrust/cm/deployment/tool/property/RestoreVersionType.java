package ru.intertrust.cm.deployment.tool.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexander Bogatyrenko on 28.07.16.
 * <p>
 * This class represents...
 */
public enum RestoreVersionType {

    CURRENT("current"),
    LAST_SUCCESSFUL("last_successful");

    private static Logger logger = LoggerFactory.getLogger(RestoreVersionType.class);

    private String typeName;

    RestoreVersionType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public static RestoreVersionType getType(String typeName) {
        for (RestoreVersionType type: RestoreVersionType.values()) {
            if (type.getTypeName().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        logger.error("Not found type by this type name - {}", typeName, new IllegalArgumentException());
        return null;
    }
}
