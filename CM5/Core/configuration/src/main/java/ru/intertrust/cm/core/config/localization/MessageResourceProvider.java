package ru.intertrust.cm.core.config.localization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Lesia Puhova
 *         Date: 04.02.2014
 *         Time: 03:13:33
 */
public class MessageResourceProvider {

    public static final String FIELD = "FIELD";
    public static final String DOMAIN_OBJECT = "DOMAIN_OBJECT";
    public static final String SEARCH_AREA = "SEARCH_AREA";
    public static final String SEARCH_DOMAIN_OBJECT = "SEARCH_DOMAIN_OBJECT";
    public static final String SEARCH_FIELD = "SEARCH_FIELD";

    public static final String DOMAIN_OBJECT_CONTEXT = "domain-object-type";

    public static final String DEFAULT_LOCALE = "DEFAULT_LOCALE";

    private static Map<String, Map<String, String>> localeToResource = new HashMap<String, Map<String, String>>();

    public static void setLocaleToResource(Map<String, Map<String, String>> localeToResource) {
        MessageResourceProvider.localeToResource = localeToResource;
    }

    @Deprecated
    public static  Map<String, String> getMessages() {
        return getMessages(DEFAULT_LOCALE);
    }

    public static  Map<String, String> getMessages(String locale) {
        return  localeToResource.get(locale);
    }

    @Deprecated
    public static String getMessage(String key) {
        return getMessage(key, DEFAULT_LOCALE);
    }

    public static String getMessage(String key, String locale) {
        if (locale == null) {
            locale = DEFAULT_LOCALE;
        }
        Map properties = localeToResource.get(locale);
        if (properties == null) {
            return key;
        }
        String localizedText = (String)properties.get(key);
        return localizedText != null ? localizedText : key;
    }

    @Deprecated
    public static String getMessage(MessageKey messageKey) {
        return getMessage(messageKey, DEFAULT_LOCALE);
    }

    public static String getMessage(MessageKey messageKey, String locale) {
        if (locale == null) {
            locale = DEFAULT_LOCALE;
        }
        Map properties = localeToResource.get(locale);
        if (properties == null) {
            return messageKey.getKey();
        }
        String displayText = (String)properties.get(createKey(messageKey.getKey(), messageKey.getClassifier(),
                messageKey.getContext()));

        if (displayText == null) {
            return messageKey.getKey();
        }
        //localize displayText itself
        return getMessage(displayText);
    }

    public static Set<String> getAvailableLocales() {
        return localeToResource.keySet();
    }

    private static String createKey(String value, String classifier, Map<String, ? extends Object> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(classifier.toUpperCase()).append("]");
        //TODO: add extra contexts here, if any.
        if (context != null && (FIELD.equals(classifier) || SEARCH_FIELD.equals(classifier) )) {
            sb.append(context.get(DOMAIN_OBJECT_CONTEXT)).append(".");
        }
        sb.append(value);
        return sb.toString();
    }

}
