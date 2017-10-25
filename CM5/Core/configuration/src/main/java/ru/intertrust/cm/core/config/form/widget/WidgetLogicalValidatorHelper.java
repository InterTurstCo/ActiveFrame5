package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 18:01
 */
public class WidgetLogicalValidatorHelper {
    private static final String FIELD_TYPE_BOOLEAN = "BOOLEAN";
    private static final String FIELD_TYPE_STRING = "STRING";
    private static final String FIELD_TYPE_LONG = "LONG";
    private static final String FIELD_TYPE_DECIMAL = "DECIMAL";
    private static final String FIELD_REFERENCE = "REFERENCE";
    private static final String REFERENCE_FIELD_CONFIG_FULL_QUALIFIED_NAME =
            "ru.intertrust.cm.core.config.ReferenceFieldConfig";

    public static boolean fieldTypeIsReference(String className) {
        return REFERENCE_FIELD_CONFIG_FULL_QUALIFIED_NAME.equalsIgnoreCase(className);
    }

    public static boolean fieldTypeIsThruReference(String fieldType) {
        return FIELD_REFERENCE.equalsIgnoreCase(fieldType);
    }

    public static  boolean fieldTypeIsBoolean(String fieldType) {
        return FIELD_TYPE_BOOLEAN.equalsIgnoreCase(fieldType);
    }

    public static boolean fieldTypeIsString(String fieldType) {
        return FIELD_TYPE_STRING.equalsIgnoreCase(fieldType);
    }

    public static boolean fieldTypeIsLong(String fieldType) {
        return FIELD_TYPE_LONG.equalsIgnoreCase(fieldType);
    }

    public static boolean fieldTypeIsDecimal(String fieldType) {
        return FIELD_TYPE_DECIMAL.equalsIgnoreCase(fieldType);
    }

    public static List<String> getFiltersFromCollectionConfig(CollectionConfig config) {
        List<String> filtersFromCollectionConfig = new ArrayList<String>();
        List<CollectionFilterConfig> filterConfigs = config.getFilters();
        for (CollectionFilterConfig filterConfig : filterConfigs) {
            filtersFromCollectionConfig.add(filterConfig.getName());
        }
        return filtersFromCollectionConfig;
    }
}
