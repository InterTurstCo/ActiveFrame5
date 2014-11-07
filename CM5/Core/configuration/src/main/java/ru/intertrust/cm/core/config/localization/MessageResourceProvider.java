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
        defaultMessages.put("validate.not-empty", "Поле ${field-name} не должно быть пустым!");
        defaultMessages.put("validate.max", "Поле ${field-name} не может быть больше чем ${value}!");
        defaultMessages.put("validate.integer", "'${value}' должно быть целым!");
        defaultMessages.put("validate.decimal", "'${value}' должно быть десятичным!");
        defaultMessages.put("validate.positive-int", "'${value}' должно быть целым положительным!");
        defaultMessages.put("validate.positive-dec", "'${value}' должно быть десятичным положительным!");
        defaultMessages.put("validate.unique-field", "Поле ${field-name} со значением '${value}' уже существует!");
        defaultMessages.put("validate.pattern", "Поле ${field-name} должно соответствовать шаблону ${pattern}!");
        defaultMessages.put("validate.length.not-equal", "Длина поля ${field-name} должна быть равна ${length}");
        defaultMessages.put("validate.length.too-small", "Длина поля ${field-name} не может быть меньше чем${min-length}");
        defaultMessages.put("validate.length.too-big", "Длина поля ${field-name} не может быть больше чем ${max-length}");
        defaultMessages.put("validate.range.too-small", "Значение поля ${field-name} не может быть меньше чем ${range-start}");
        defaultMessages.put("validate.range.too-big", "Значение поля ${field-name} не может быть больше чем ${range-end}");
        defaultMessages.put("validate.precision", "Значение поля ${field-name} должно иметь точность ${precision}");
        defaultMessages.put("validate.scale", "Значение поля ${field-name} должно иметь ${scale} знаков после запятой");
    }

//    static {
//        defaultMessages.put("validate.not-empty", "Field ${field-name} cannot be empty!");
//        defaultMessages.put("validate.max", "Field ${field-name} cannot be greater than ${value}!");
//        defaultMessages.put("validate.integer", "'${value}' is not valid integer number!");
//        defaultMessages.put("validate.decimal", "'${value}' is not valid decimal number!");
//        defaultMessages.put("validate.positive-int", "'${value}' is not valid positive integer number!");
//        defaultMessages.put("validate.positive-dec", "'${value}' is not valid positive decimal number!");
//        defaultMessages.put("validate.unique-field", "Field ${field-name} with value '${value}' already exists!");
//        defaultMessages.put("validate.pattern", "Field ${field-name} should match pattern ${pattern}!");
//        defaultMessages.put("validate.length.not-equal", "Length of field ${field-name} should be equal to ${length}");
//        defaultMessages.put("validate.length.too-small", "Length of field ${field-name} cannot be less than ${min-length}");
//        defaultMessages.put("validate.length.too-big", "Length of field ${field-name} cannot be greater than ${max-length}");
//        defaultMessages.put("validate.range.too-small", "Value of field ${field-name} cannot be less than ${range-start}");
//        defaultMessages.put("validate.range.too-big", "Value of field ${field-name} cannot be greater than ${range-end}");
//        defaultMessages.put("validate.precision", "Value of field ${field-name} should have precision ${precision}");
//        defaultMessages.put("validate.scale", "Value of field ${field-name} should have scale ${scale}");
//    }

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

    public static String getMessage(String messageKey) {
        return getMessages().get(messageKey);
    }
}
