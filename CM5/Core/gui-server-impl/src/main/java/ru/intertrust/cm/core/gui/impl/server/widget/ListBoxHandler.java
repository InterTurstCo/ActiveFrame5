package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.ListBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.MultiObjectWidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis Mitavskiy
 *         Date: 15.10.13
 *         Time: 16:25
 */
@ComponentName("list-box")
public class ListBoxHandler extends MultiObjectWidgetHandler {
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected ConfigurationService configurationService;

    @Override
    public ListBoxState getInitialState(WidgetContext context) {
        ListBoxConfig widgetConfig = context.getWidgetConfig();

        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        ArrayList<DomainObject> objectReferencingField = context.getFormObjects().getObjects(fieldPath.getParent()).getDomainObjects(); // todo we can't find type when DO is empty, thus - fix. we got to push type from outside
        Id baseObjectId = objectReferencingField.get(0).getId();

        String field = fieldPath.getLastElement();
        String[] referencingTypeAndField = field.split("\\^");
        String referencingType = referencingTypeAndField[0];
        String referencingField = referencingTypeAndField[1];
        List<DomainObject> listToDisplay = crudService.findAll(referencingType);
        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();

        ListBoxState result = new ListBoxState();
        result.setListValues(idDisplayMapping);

        if (listToDisplay == null) {
            return result;
        }

        Pattern pattern = Pattern.compile("\\{\\w+\\}");
        Matcher matcher = pattern.matcher(widgetConfig.getPatternConfig().getValue());
        for (DomainObject domainObject : listToDisplay) {
            String format = format(domainObject, matcher);
            idDisplayMapping.put(domainObject.getId(), format);
        }

        ArrayList<Id> selectedIds = new ArrayList<>();
        for (DomainObject domainObject : listToDisplay) {
            Id referenceToBaseObject = domainObject.getReference(referencingField);
            if (baseObjectId.equals(referenceToBaseObject)) {
                selectedIds.add(domainObject.getId());
            }
        }

        result.setSelectedIds(selectedIds);

        return result;
    }

    private String format(DomainObject domainObject, Matcher matcher) {
        StringBuffer replacement = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            Value value = domainObject.getValue(fieldName);
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
}
