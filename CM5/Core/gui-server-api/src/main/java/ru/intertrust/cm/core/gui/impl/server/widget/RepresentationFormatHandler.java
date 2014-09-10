package ru.intertrust.cm.core.gui.impl.server.widget;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NumberFormatConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;


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

    private String getDisplayValue(String field, Value value, FormattingConfig formattingConfig) {
        StringBuilder displayValue = new StringBuilder();
        if (value != null) {
            Object primitiveValue = value.get();


            if (value instanceof LongValue || value instanceof DecimalValue) {
                displayValue.append(getNumberDisplayValue(field, primitiveValue, formattingConfig));
            } else if (primitiveValue != null) {
                SimpleDateFormat dateFormatter = prepareSimpleDateFormat(field, formattingConfig);
                if (value instanceof DateTimeValue) {
                    DateValueConverter<DateTimeValue> dateTimeValueDateValueConverter = new DateTimeValueConverter();
                    String timeZoneId = getTimeZoneId(field, formattingConfig);
                    DateTimeContext dateTimeContext = dateTimeValueDateValueConverter.valueToContext((DateTimeValue)value, timeZoneId,dateFormatter);
                    displayValue.append(dateTimeContext.getDateTime());
                } else if (value instanceof TimelessDateValue) {

                    DateValueConverter<TimelessDateValue> dateValueConverter = new TimelessDateValueConverter();
                    String timeZoneId = getTimeZoneId(field, formattingConfig);
                    DateTimeContext dateTimeContext = dateValueConverter.valueToContext((TimelessDateValue)value, timeZoneId, dateFormatter);
                    displayValue.append(dateTimeContext.getDateTime());
                } else if (value instanceof DateTimeWithTimeZoneValue) {
                    DateValueConverter<DateTimeWithTimeZoneValue> dateValueConverter = new DateTimeWithTimezoneValueConverter();
                    String timeZoneId = getTimeZoneId(field, formattingConfig);
                    DateTimeContext dateTimeContext = dateValueConverter.valueToContext((DateTimeWithTimeZoneValue)value, timeZoneId, dateFormatter);
                    displayValue.append(dateTimeContext.getDateTime());

                } else {
                    displayValue.append(primitiveValue.toString());
                }
            }
        }

        return displayValue.toString();
    }

    private String getTimeZoneId(String field, FormattingConfig formattingConfig){
        if(formattingConfig == null || formattingConfig.getDateFormatConfig() == null || !
                isFormatterUsedForCurrentField(field, formattingConfig.getDateFormatConfig().getFieldsPathConfig())) {
            return  GuiContext.get().getUserInfo().getTimeZoneId();
        }
        return formattingConfig.getDateFormatConfig().getTimeZoneConfig().getId();
    }

    private SimpleDateFormat prepareSimpleDateFormat(String field, FormattingConfig formattingConfig) {
        if(formattingConfig != null && formattingConfig.getDateFormatConfig() != null &&
                isFormatterUsedForCurrentField(field, formattingConfig.getDateFormatConfig().getFieldsPathConfig())) {
          return new SimpleDateFormat(formattingConfig.getDateFormatConfig().getPattern());
        }
        return new SimpleDateFormat(ModelUtil.DEFAULT_DATE_PATTERN);
    }

    private String getNumberDisplayValue(String field, Object primitiveValue, FormattingConfig formattingConfig) {
        String value = formattingConfig == null ? getNumberDisplayWithoutFormatter(primitiveValue)
                : getNumberDisplayValueProbablyWithFormatting(field, primitiveValue, formattingConfig.getNumberFormatConfig());
        return value;

    }

    private String getNumberDisplayValueProbablyWithFormatting(String field, Object primitiveValue,
                                                               NumberFormatConfig numberFormatConfig) {
       String value = isFormatterUsedForCurrentField(field, numberFormatConfig.getFieldsPathConfig()) ?
               getNumberDisplayWithFormatting(primitiveValue, numberFormatConfig) :
               getNumberDisplayWithoutFormatter(primitiveValue);
       return value;
    }

    private String getNumberDisplayWithoutFormatter(Object primitiveValue) {
        String value = primitiveValue == null ? "0" : primitiveValue.toString();
        return value;
    }

    private String getNumberDisplayWithFormatting(Object primitiveValue, NumberFormatConfig numberFormatConfig) {
        DecimalFormat decimalFormat = new DecimalFormat(numberFormatConfig.getPattern());
        String value = primitiveValue == null ? decimalFormat.format(0) : decimalFormat.format(primitiveValue);
        return value;
    }

    private boolean isFormatterUsedForCurrentField(String field, FieldPathsConfig fieldPathsConfig) {
        List<FieldPathConfig> fieldPathConfigs = fieldPathsConfig.getFieldPathConfigsList();
        for (FieldPathConfig fieldPathConfig : fieldPathConfigs) {
            if (fieldPathConfig.getValue().equalsIgnoreCase(field)) {
                return true;
            }
        }
        return false;
    }

    private String getDisplayValue(String fieldName, IdentifiableObject identifiableObject, FormattingConfig formattingConfig) {

        Value value = identifiableObject.getValue(fieldName);
        return getDisplayValue(fieldName, value, formattingConfig);

    }
}


