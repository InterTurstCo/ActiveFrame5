package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import static ru.intertrust.cm.core.gui.impl.server.widget.util.WidgetRepresentationUtil.getDisplayValue;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@ComponentName("representation-updater")
public class RepresentationFormatHandler implements FormatHandler {
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected CrudService crudService;

    @Deprecated
    public RepresentationResponse getRepresentation(Dto inputParams) {
        RepresentationRequest request = (RepresentationRequest) inputParams;
        String selectionPattern = request.getPattern();
        Matcher matcher = pattern.matcher(selectionPattern);
        final List<Id> ids = request.getIds();
        Iterator<Id> iterator = ids.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            Id id = iterator.next();
            DomainObject domainObject = crudService.find(id);
            String representation = formatWithSplit(domainObject, matcher, null);

            sb.append(representation);
            if (iterator.hasNext()) {
                sb.append("; ");
            }
        }
        RepresentationResponse response = new RepresentationResponse(sb.toString());
        return response;
    }

    public String format(String selectionPattern, List<Id> ids, FormattingConfig formattingConfig) {

        Matcher matcher = pattern.matcher(selectionPattern);
        Iterator<Id> iterator = ids.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            Id id = iterator.next();
            DomainObject domainObject = crudService.find(id);
            String representation = formatWithSplit(domainObject, matcher, formattingConfig);

            sb.append(representation);
            if (iterator.hasNext()) {
                sb.append("; ");
            }
        }

        return sb.toString();
    }

    public RepresentationResponse getRepresentationForOneItem(Dto inputParams) {
        RepresentationRequest request = (RepresentationRequest) inputParams;
        String selectionPattern = request.getPattern();
        FormattingConfig formattingConfig = request.getFormattingConfig();
        Matcher matcher = pattern.matcher(selectionPattern);
        List<Id> ids = request.getIds();
        Id id = ids.get(0);
        DomainObject domainObject = crudService.find(id);
        String representation = format(domainObject, matcher, formattingConfig);
        RepresentationResponse response = new RepresentationResponse(id, representation);
        return response;
    }

    private String formatWithSplit(IdentifiableObject identifiableObject, Matcher matcher, FormattingConfig formattingConfig) {
        StringBuffer replacement = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = fieldName.contains(".") || fieldName.contains("|") ? getFormattedReferenceValueByFieldPath(fieldName, identifiableObject, true, formattingConfig) :
                    getDisplayValue(fieldName, identifiableObject, formattingConfig);
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    public String format(Value value, Matcher matcher, FormattingConfig formattingConfig) {
        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = getDisplayValue(fieldName, value, formattingConfig);
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    @Override
    public String format(DateBoxState state, Matcher matcher, FormattingConfig formattingConfig) {
        StringBuffer replacement = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            ValueEditingWidgetHandler valueEditingWidgetHandler = (ValueEditingWidgetHandler) applicationContext.getBean("date-box");
            Value value = valueEditingWidgetHandler.getValue(state);
            String displayValue = getDisplayValue(fieldName, value, formattingConfig);
            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }


    public String format(DomainObject domainObject, Matcher matcher, FormattingConfig formattingConfig) {
        return format((IdentifiableObject) domainObject, matcher, formattingConfig);
    }


    @Override
    public String format(WidgetContext context, Matcher matcher, FormattingConfig formattingConfig) {
        StringBuffer replacement = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();

            FieldPath fieldPath = new FieldPath(group.substring(1, group.length() - 1));
            final String displayValueUnescaped;
            if ("id".equals(fieldPath.getFieldName())) {
                final DomainObject rootObject = context.getFormObjects().getRootDomainObject();
                displayValueUnescaped = (rootObject == null || rootObject.getId() == null)
                        ? ""
                        : rootObject.getId().toStringRepresentation();
            } else {
                Value value = context.getValue(fieldPath);
                displayValueUnescaped = getDisplayValue(fieldPath.getFieldName(), value, formattingConfig);
            }
            String displayValue = displayValueUnescaped.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
            matcher.appendReplacement(replacement, displayValue);
        }

        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    public String format(IdentifiableObject identifiableObject, Matcher matcher, FormattingConfig formattingConfig) {

        StringBuffer replacement = new StringBuffer();

        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            String displayValue = fieldName.contains(".") || fieldName.contains("|")
                    ? getFormattedReferenceValueByFieldPath(fieldName, identifiableObject, false, formattingConfig)
                    : getDisplayValue(fieldName, identifiableObject, formattingConfig);

            matcher.appendReplacement(replacement, displayValue);
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    private String getFormattedReferenceValueByFieldPath(String fieldName, IdentifiableObject identifiableObject,
                                                         boolean skipFirstElement, FormattingConfig formattingConfig) {
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
                    return getDisplayValue(iterator.getValue(), tempIdentifiableObject, formattingConfig);
                case DIRECT_REFERENCE:
                    Id referenceId = tempIdentifiableObject.getReference(iterator.getValue());
                    if(referenceId != null){
                        tempIdentifiableObject = crudService.find(referenceId);
                    }
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


}


