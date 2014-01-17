package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:58
 */
public abstract class WidgetHandler implements ComponentHandler {
    public abstract <T extends WidgetState> T getInitialState(WidgetContext context);

    public abstract Value getValue(WidgetState state);

    protected ArrayList<String> format(List<DomainObject> listToDisplay, String displayPattern) {
        Pattern pattern = Pattern.compile("\\{\\w+\\}");
        Matcher matcher = pattern.matcher(displayPattern);
        ArrayList<String> displayValues = new ArrayList<>(listToDisplay.size());
        for (DomainObject domainObject : listToDisplay) {
            displayValues.add(format(domainObject, matcher));
        }
        return displayValues;
    }

    protected String format(DomainObject domainObject, Matcher matcher) {
        return format((IdentifiableObject) domainObject, matcher);
    }

    protected String format(IdentifiableObject identifiableObject, Matcher matcher) {
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

    protected void appendDisplayMappings(List<DomainObject> listToDisplay, String displayPattern,
                                         Map<Id, String> idDisplayMapping) {
        ArrayList<String> displayValues = format(listToDisplay, displayPattern);
        for (int i = 0; i < listToDisplay.size(); i++) {
            DomainObject domainObject = listToDisplay.get(i);
            idDisplayMapping.put(domainObject.getId(), displayValues.get(i));
        }
    }

    protected boolean isSingleChoice(WidgetContext context, boolean singleChoiceFromConfig) {
       FieldPath[] fieldPaths = context.getFieldPaths();
       Boolean singleChoiceAnalyzed = null;
       for (FieldPath fieldPath : fieldPaths) {
           if (singleChoiceAnalyzed != null) {
              if (singleChoiceAnalyzed.booleanValue() != (fieldPath.isOneToOneReference() || fieldPath.isField())){
                  throw new GuiException("Multiply fieldPaths should be all reference type or all backreference type");
              }

           }
            singleChoiceAnalyzed = fieldPath.isOneToOneReference() || fieldPath.isField();
       }
       return singleChoiceAnalyzed.booleanValue() == true ? true : singleChoiceFromConfig;

    }
}
