package ru.intertrust.cm.core.config.localization;

import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 03.12.14
 *         Time: 18:03
 */
public interface LocalizationLoader {

    interface Remote extends LocalizationLoader {}

    public Map<String, Map<String, String>> load();

}
