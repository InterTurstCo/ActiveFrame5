package ru.intertrust.cm.deployment.tool.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by alexander on 01.08.16.
 */
public enum AttachmentType {

    STORAGE("attachment.storage", "attch"),
    TEMP_STORAGE("attachment.temp.storage", "tmp"),
    PUBLICATION_STORAGE("attachment.publication.storage", "pbc");

    private static Logger logger = LoggerFactory.getLogger(AttachmentType.class);

    private String typeValue;
    private String shortName;

    AttachmentType(String typeValue, String shortName) {
        this.typeValue = typeValue;
        this.shortName = shortName;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public String getShortName() {
        return shortName;
    }

    public AttachmentType getType(String value) {
        for (AttachmentType type: AttachmentType.values()) {
            if (type.getTypeValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        logger.error("Not found type by this type value - {}", value, new IllegalArgumentException());
        return null;
    }
}
