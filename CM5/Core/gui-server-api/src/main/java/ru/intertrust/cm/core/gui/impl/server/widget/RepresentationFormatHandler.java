package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
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

    private String formatWithSplit(IdentifiableObject identifiableObject, Matcher matcher) {
        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = fieldName.contains(".") || fieldName.contains("|") ? getFormattedReferenceValueByFieldPath(fieldName, identifiableObject, true) :
                    getDisplayValue(fieldName, identifiableObject);
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    public String format(DomainObject domainObject, Matcher matcher) {
        return format((IdentifiableObject) domainObject, matcher);
    }

    @Override
    public String format(WidgetContext context, Matcher matcher, String allValuesEmpty) {
        StringBuffer replacement = new StringBuffer();
        while (matcher.find()) {
        String group = matcher.group();
        FieldPath fieldPath = new FieldPath(group.substring(1, group.length() - 1));
        Value value = context.getValue(fieldPath);
        String displayValueUnescaped = getDisplayValue(value);

        String displayValue = displayValueUnescaped.replaceAll( "\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
        matcher.appendReplacement(replacement, displayValue);
        }

        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.length() == 0 ? allValuesEmpty : replacement.toString();
    }

    public String format(IdentifiableObject identifiableObject, Matcher matcher) {

        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = fieldName.contains(".") || fieldName.contains("|")
                    ? getFormattedReferenceValueByFieldPath(fieldName, identifiableObject, false)
                    : getDisplayValue(fieldName, identifiableObject);

            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    private String getFormattedReferenceValueByFieldPath(String fieldName, IdentifiableObject identifiableObject,
                                                         boolean skipFirstElement) {
        StringBuilder displayValue = new StringBuilder();
        IdentifiableObject tempIdentifiableObject = identifiableObject;
        PatternIterator iterator = new PatternIterator(fieldName);
        if (skipFirstElement) {
            iterator.moveToNext();
        }
        while (iterator.moveToNext()) {
            PatternIterator.ReferenceType type = iterator.getType();
            switch (type) {
                case FIELD:
                    return getDisplayValue(iterator.getValue(), tempIdentifiableObject);
                case DIRECT_REFERENCE:
                    tempIdentifiableObject = crudService.find(tempIdentifiableObject.
                            getReference(iterator.getValue()));
                    break;
                case BACK_REFERENCE_ONE_TO_ONE:
                    Id id = identifiableObject.getId();
                    String domainObjectType = crudService.getDomainObjectType(id);
                    iterator.moveToNext();
                    List<DomainObject> linkedObjects = crudService.findLinkedDomainObjects(id, domainObjectType, iterator.getValue());
                    if (!linkedObjects.isEmpty()) {
                        tempIdentifiableObject = linkedObjects.get(0);

                    }
                    break;
            }

        }
        return displayValue.toString();
    }

    private String getDisplayValue(Value value) {
        StringBuilder displayValue = new StringBuilder();
        if (value != null) {
            Object primitiveValue = value.get();
            if (primitiveValue == null) {
                if (value instanceof LongValue || value instanceof DecimalValue) {
                    displayValue.append("0");
                }
            } else {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
                if (value instanceof DateTimeValue) {
                    displayValue.append(dateFormatter.format(primitiveValue));
                } else if (value instanceof TimelessDateValue) {
                    TimelessDate timelessDate = ((TimelessDateValue) value).get();
                    TimeZone timeZone = TimeZone.getTimeZone(GuiContext.get().getUserInfo().getTimeZoneId());
                    Calendar calendar = GuiServerHelper.timelessDateToCalendar(timelessDate, timeZone);
                    displayValue.append(dateFormatter.format(calendar.getTime()));
                } else if (value instanceof DateTimeWithTimeZoneValue) {
                    DateTimeWithTimeZone withTimeZone = ((DateTimeWithTimeZoneValue) value).get();
                    Calendar calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(withTimeZone);
                    displayValue.append(dateFormatter.format(calendar.getTime()));
                } else {
                    displayValue.append(primitiveValue.toString());
                }
            }
        }

        return displayValue.toString();
    }


    private String getDisplayValue(String fieldName, IdentifiableObject identifiableObject) {

        Value value = identifiableObject.getValue(fieldName);
        return getDisplayValue(value);

    }
}


