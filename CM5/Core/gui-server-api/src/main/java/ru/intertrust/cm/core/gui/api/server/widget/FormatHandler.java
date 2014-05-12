package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 06.05.2014.
 */
public interface FormatHandler extends ComponentHandler {
    public static final String FIELD_PLACEHOLDER_PATTERN = "\\{\\w+\\}|\\{\\w+\\.\\w+\\}";
    public static final Pattern pattern = Pattern.compile(FIELD_PLACEHOLDER_PATTERN);
    public String format(IdentifiableObject identifiableObject, Matcher matcher);
    public String format(DomainObject domainObject, Matcher matcher);
/*
    public static final String FIELD_PLACEHOLDER_PATTERN = "\\{\\w+\\}";
    public static final Pattern pattern = Pattern.compile(FIELD_PLACEHOLDER_PATTERN);

    public static String format(DomainObject domainObject, Matcher matcher) {
        return format((IdentifiableObject) domainObject, matcher);
    }

    public static String format(IdentifiableObject identifiableObject, Matcher matcher) {

        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);

            Value value = identifiableObject.getValue(fieldName);
            String displayValue = "";
            if (value != null) {
                Object primitiveValue = value.get();
                if (primitiveValue == null) {
                    if (value instanceof LongValue || value instanceof DecimalValue) {
                        displayValue = "0";
                    }
                } else {
                    displayValue = primitiveValue.toString();
                }
            }
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    public static String format(IdentifiableObject identifiableObject, Matcher matcher, CrudService crudService) {

        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = fieldName.contains(".") ? getFormattedReferenceValue(fieldName, identifiableObject, crudService) :
                    getDisplayValue(fieldName, identifiableObject);
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    private static String getFormattedReferenceValue(String fieldNameWithDoel, IdentifiableObject identifiableObject,
                                                     CrudService crudService) {
        String displayValue = "";
        String[] parts = fieldNameWithDoel.split("\\.");
        int length = parts.length;
        IdentifiableObject tempIdentifiableObject = identifiableObject;
        for (int i = 0; i < length; i++) {
            String fieldName = parts[i];
            DomainObject domainObject = crudService.find(tempIdentifiableObject.getReference(fieldName));
            if (i + 1 == length) {
                String primitiveFieldName = parts[i + 1];
                displayValue = getDisplayValue(primitiveFieldName, domainObject);
                return displayValue;
            } else {
                tempIdentifiableObject = domainObject;
            }

        }
        return displayValue;


    }

    private static String getDisplayValue(String fieldName, IdentifiableObject identifiableObject) {
        Value value = identifiableObject.getValue(fieldName);
        String displayValue = "";
        if (value != null) {
            Object primitiveValue = value.get();
            if (primitiveValue == null) {
                if (value instanceof LongValue || value instanceof DecimalValue) {
                    displayValue = "0";
                }
            } else {
                displayValue = primitiveValue.toString();
            }
        }
        return displayValue;
    }*/
}
