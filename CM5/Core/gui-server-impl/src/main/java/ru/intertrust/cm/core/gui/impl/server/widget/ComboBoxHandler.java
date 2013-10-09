package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldTypeConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.ComboBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("combo-box")
public class ComboBoxHandler extends WidgetHandler {
    @Override
    public ComboBoxData getInitialDisplayData(WidgetContext context) {
        ComboBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        DomainObject objectContainingField = context.getFormObjects().getObject(fieldPath.createFieldPathWithoutLastElement());
        String field = fieldPath.getLastElement();
        String objectType = objectContainingField.getTypeName();
        ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) getConfigurationService().getFieldConfig(objectType, field);
        List<ReferenceFieldTypeConfig> referenceFieldTypes = fieldConfig.getTypes();
        if (referenceFieldTypes.size() > 1) {
            throw new IllegalArgumentException("Combo-box is not supposed to be used with multi-typed fields");
        }
        // todo: find LINKED: only cities of that country
        // List<DomainObject> listToDisplay = getCrudService().findLinkedDomainObjects(objectContainingField.getId(), "city", "country");
        List<DomainObject> listToDisplay = getCrudService().findAll(referenceFieldTypes.get(0).getName());
        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();
        idDisplayMapping.put(null, "");

        ComboBoxData result = new ComboBoxData();
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

        Id selectedId = context.getFieldPathPlainValue();

        result.setId(selectedId);

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
