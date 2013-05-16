package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;

/**
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 11:27 AM
 */
public class DataStructureNamingHelper {

    public static String getSqlName(BusinessObjectConfig businessObjectConfig) {
        return convertToSqlFormat(businessObjectConfig.getName());
    }

    public static String getSqlName(FieldConfig fieldConfig) {
        return convertToSqlFormat(fieldConfig.getName());
    }

    public static String getSqlName(ReferenceFieldConfig referenceFieldConfig) {
        return convertToSqlFormat(referenceFieldConfig.getType());
    }

    private static String convertToSqlFormat(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        name = name.trim();

        if(name.isEmpty()) {
            throw new IllegalArgumentException("Name is empty");
        }

        return name.replace(' ', '_').toUpperCase();
    }
}
