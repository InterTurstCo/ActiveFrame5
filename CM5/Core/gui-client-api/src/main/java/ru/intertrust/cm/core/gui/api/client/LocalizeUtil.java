package ru.intertrust.cm.core.gui.api.client;

/**
 * @author Lesia Puhova
 *         Date: 27.02.2015
 *         Time: 19:13
 */
public class LocalizeUtil {

    private LocalizeUtil(){} //non-instantiable

    /**
        Returns localized message for current locale
     */
    public static String get(String key) {
        String value = Application.getInstance().getLocalizedResources().get(key);
        return value != null ? value : key;
    }


}
