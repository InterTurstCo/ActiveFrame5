package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.gui.form.FormConfig;

import java.util.Collections;
import java.util.List;

/**
 * Служебный класс, содержащий нулевые значения классов для оптимизации кэша при возвращении нулевых значений
 */
public class NullValues {

    public static AccessMatrixStatusConfig ACCESS_MATRIX_STATUS_CONFIG = new AccessMatrixStatusConfig();
    public static AccessMatrixConfig ACCESS_MATRIX_CONFIG = new AccessMatrixConfig();
    public static FieldConfig FIELD_CONFIG = new StringFieldConfig();
    public static FormConfig FORM_CONFIG = new FormConfig();
    public static DomainObjectTypeConfig DOMAIN_OBJECT_TYPE_CONFIG = new DomainObjectTypeConfig();
    public static String STRING = new String();
    public static List LIST = Collections.emptyList();

    public static boolean isNull(AccessMatrixStatusConfig accessMatrixStatusConfig) {
        return accessMatrixStatusConfig == null || accessMatrixStatusConfig == ACCESS_MATRIX_STATUS_CONFIG;
    }

    public static AccessMatrixStatusConfig convertNull(AccessMatrixStatusConfig accessMatrixStatusConfig) {
        return NullValues.isNull(accessMatrixStatusConfig) ? null : accessMatrixStatusConfig;
    }

    public static boolean isNull(AccessMatrixConfig accessMatrixConfig) {
        return accessMatrixConfig == null || accessMatrixConfig == ACCESS_MATRIX_CONFIG;
    }

    public static AccessMatrixConfig convertNull(AccessMatrixConfig accessMatrixConfig) {
        return NullValues.isNull(accessMatrixConfig) ? null : accessMatrixConfig;
    }

    public static boolean isNull(String string) {
        return string == null || STRING.equals(string);
    }

    public static String convertNull(String string) {
        return NullValues.isNull(string) ? null : string;
    }

    public static boolean isNull(FormConfig formConfig) {
        return formConfig == null || formConfig == FORM_CONFIG;
    }

    public static FormConfig convertNull(FormConfig formConfig) {
        return NullValues.isNull(formConfig) ? null : formConfig;
    }

    public static <T> boolean isNull(List<T> list) {
        return list == null || list == LIST;
    }

    public static <T> List<T> convertNull(List<T> list) {
        return NullValues.isNull(list) ? null : list;
    }

    public static boolean isNull(DomainObjectTypeConfig domainObjectTypeConfig) {
        return domainObjectTypeConfig == null || domainObjectTypeConfig == DOMAIN_OBJECT_TYPE_CONFIG;
    }

    public static DomainObjectTypeConfig convertNull(DomainObjectTypeConfig domainObjectTypeConfig) {
        return NullValues.isNull(domainObjectTypeConfig) ? null : domainObjectTypeConfig;
    }
}
