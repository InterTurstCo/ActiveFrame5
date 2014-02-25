package ru.intertrust.cm.core.config.localization;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 04.02.2014
 *         Time: 03:13:33
 */
public class MessageResourceProvider {

    private static Map<String, String> defaultMessages = new HashMap<String, String>();

    private static final String DEFAULT_LOCALE = "default";

    static {
        defaultMessages.put("validate.not-empty", "Field ${field-name} cannot be empty!");
        defaultMessages.put("validate.max", "Field ${field-name} cannot be greater than ${value}!");
        defaultMessages.put("validate.integer", "'${value}' is not valid integer number!");
        defaultMessages.put("validate.decimal", "'${value}' is not valid decimal number!");
        defaultMessages.put("validate.positive-int", "'${value}' is not valid positive integer number!");
        defaultMessages.put("validate.positive-dec", "'${value}' is not valid positive decimal number!");
        defaultMessages.put("validate.unique-field", "Field ${field-name} with value '${value}' already exists!");
        defaultMessages.put("validate.pattern", "Field ${field-name} should match pattern ${pattern}!");
    }

    private static Map<String, Map<String, String>> localeToResource = new HashMap<String, Map<String, String>>();

    public static  Map<String, String> getMessages() {
        return getMessages(DEFAULT_LOCALE);
    }

    public static  Map<String, String> getMessages(String locale) {
        if (localeToResource.get(locale) != null) {
            return  localeToResource.get(locale);
        }

       //todo: load from property file for the given locale or for default locale if not found
        Map<String, String> localizedMessages = new HashMap<>();

        Map<String, String> messages = new HashMap<String, String>();
        messages.putAll(defaultMessages);
        messages.putAll(localizedMessages);

        localeToResource.put(locale, messages);
        return messages;
    }

}
