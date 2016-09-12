package ru.intertrust.cm.core.config.localization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final String SEARCH_AREA_RESULT = "search-result-label";
    public static final String SEARCH_AREA_FILERTS = "search-filters-label";


    public static final String DOMAIN_OBJECT_CONTEXT = "domain-object-type";

    private static Map<String, Map<String, String>> localeToResource = new ConcurrentHashMap<>();
    private static String defaultLocale;

    public static void setLocaleToResource(Map<String, Map<String, String>> localeToResource) {
        MessageResourceProvider.localeToResource = localeToResource;
        MessageResourceProvider.defaultLocale = findDefaultLocale();
    }

    public static String getDefaultLocale() {
        return defaultLocale;
    }

    public static  Map<String, String> getMessages(String locale) {
        if (locale == null) {
            return new HashMap<>();
        }
        Map<String, String> result = localeToResource.get(locale);
        return  result != null ? result : new HashMap<String, String>();
    }

    public static String getMessage(String key, String locale) {
       return getMessage(key, locale, key);
    }

    public static String getMessage(String key, String locale, String defaultValue) {
        Map properties = localeToResource.get(locale);
        if (properties == null) {
            return defaultValue;
        }
        String localizedText = (String)properties.get(key);
        return localizedText != null ? localizedText : defaultValue;
    }

    public static String getMessage(MessageKey messageKey, String locale) {

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
        return getMessage(displayText, locale);
    }

    public static Set<String> getAvailableLocales() {
        return localeToResource.keySet();
    }

    private static String findDefaultLocale() {
        final Set<String> locales = MessageResourceProvider.getAvailableLocales();
        if (locales == null || locales.isEmpty()) {
            return null;
        }
        if (locales.contains("ru")) {
            return "ru";
        }
        if (locales.contains("en")) {
            return "en";
        }
        for (String locale : locales) {
            if (locale.startsWith("ru")) {
                return locale;
            }
        }
        for (String locale : locales) {
            if (locale.startsWith("en")) {
                return locale;
            }
        }
        return locales.iterator().next();
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
