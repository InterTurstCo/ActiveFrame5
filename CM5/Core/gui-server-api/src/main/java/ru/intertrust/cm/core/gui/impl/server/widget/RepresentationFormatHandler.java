package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;

import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@ComponentName("representation-updater")
public class RepresentationFormatHandler implements FormatHandler {

    @Autowired
    protected CrudService crudService;

    private static final String EMPTY_VALUE = "";

    public RepresentationResponse getRepresentation(Dto inputParams) {
        RepresentationRequest request = (RepresentationRequest) inputParams;
        String selectionPattern = request.getPattern();
        Matcher matcher = pattern.matcher(selectionPattern);
        List<Id> ids = request.getIds();
        Iterator<Id> iterator = ids.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            Id id = iterator.next();
            DomainObject domainObject = crudService.find(id);
            String representation = formatWithSplit(domainObject, matcher);

            sb.append(representation);
            if (iterator.hasNext()) {
                sb.append("; ");
            }
        }
        RepresentationResponse response = new RepresentationResponse(sb.toString());
        return response;
    }

    public RepresentationResponse getRepresentationForOneItem(Dto inputParams) {
        RepresentationRequest request = (RepresentationRequest) inputParams;
        String selectionPattern = request.getPattern();
        Matcher matcher = pattern.matcher(selectionPattern);
        List<Id> ids = request.getIds();
        Id id = ids.get(0);
        DomainObject domainObject = crudService.find(id);
        String representation = format(domainObject, matcher);
        RepresentationResponse response = new RepresentationResponse(id, representation);
        return response;
    }

    private String formatWithSplit(IdentifiableObject identifiableObject, Matcher matcher){
        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = fieldName.contains(".") ? getFormattedReferenceValueBySplittedPattern(fieldName, identifiableObject) :
                    getDisplayValue(fieldName, identifiableObject);
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    public  String format(DomainObject domainObject, Matcher matcher) {
        return format((IdentifiableObject) domainObject, matcher);
    }

    public String format(IdentifiableObject identifiableObject, Matcher matcher) {

        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = fieldName.contains(".") ? getFormattedReferenceValue(fieldName, identifiableObject) :
                    getDisplayValue(fieldName, identifiableObject);
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    private String getFormattedReferenceValue(String fieldNameWithDoel, IdentifiableObject identifiableObject) {

        String displayValue = "";
        String[] parts = fieldNameWithDoel.split("\\.");
        int length = parts.length;
        IdentifiableObject tempIdentifiableObject = identifiableObject;
        for (int i = 0; i < length; i++) {
            String fieldName = parts[i];
            DomainObject domainObject = crudService.find(tempIdentifiableObject.getReference(fieldName));
            if (i + 2 == length) {
                String primitiveFieldName = parts[i + 1];
                displayValue = getDisplayValue(primitiveFieldName, domainObject);
                return displayValue;
            } else {
                tempIdentifiableObject = domainObject;
            }

        }
        return displayValue;

    }

    private String getFormattedReferenceValueBySplittedPattern(String fieldNameWithDoel, IdentifiableObject identifiableObject) {

        String displayValue = EMPTY_VALUE;
        String[] parts = fieldNameWithDoel.split("\\.");
        int length = parts.length;
        IdentifiableObject tempIdentifiableObject = identifiableObject;
        for (int i = 1; i < length; i++) {
            String fieldName = parts[i];
            if (i + 1 == length) {
                String primitiveFieldName = parts[i];
                displayValue = getDisplayValue(primitiveFieldName, tempIdentifiableObject);
                return displayValue;
            } else {
                DomainObject domainObject = crudService.find(tempIdentifiableObject.getReference(fieldName));
                tempIdentifiableObject = domainObject;
            }

        }
        return displayValue;

    }

    private String getDisplayValue(String fieldName, IdentifiableObject identifiableObject) {
        Value value = identifiableObject.getValue(fieldName);
        String displayValue = EMPTY_VALUE;
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
    }
}


