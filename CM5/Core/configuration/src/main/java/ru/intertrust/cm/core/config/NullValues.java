package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.gui.form.FormConfig;

/**
 * Служебный класс, содержащий нулевые значения классов для оптимизации кэша при возвращении нулевых значений
 */
public class NullValues {

    public static AccessMatrixStatusConfig ACCESS_MATRIX_STATUS_CONFIG = new AccessMatrixStatusConfig();
    public static FormConfig FORM_CONFIG = new FormConfig();
    public static String STRING = new String();

    public static boolean isNull(AccessMatrixStatusConfig accessMatrixStatusConfig) {
        return accessMatrixStatusConfig == null || accessMatrixStatusConfig == ACCESS_MATRIX_STATUS_CONFIG;
    }

    public static boolean isNull(String string) {
        return string == null || string == STRING;
    }

    public static boolean isNull(FormConfig formConfig) {
        return formConfig == null || formConfig == FORM_CONFIG;
    }
}
