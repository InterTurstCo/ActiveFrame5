package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldTypeConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.ComboBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormData;
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
    public ComboBoxData getInitialDisplayData(WidgetContext context, FormData formData) {
        ComboBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        DomainObject rootObjectForComboBoxField = formData.getFieldPathObject(fieldPath.createFieldPathWithoutLastElement());
        String field = fieldPath.getLastElement();
        String rootObjectType = rootObjectForComboBoxField.getTypeName();
        ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) getConfigurationService().getFieldConfig(rootObjectType, field);
        List<ReferenceFieldTypeConfig> referenceFieldTypes = fieldConfig.getTypes();
        if (referenceFieldTypes.size() > 1) {
            throw new IllegalArgumentException("Combo-box is not supposed to be used with multi-typed fields");
        }
        List<DomainObject> listToDisplay = getCrudService().findAll(referenceFieldTypes.get(0).getName());
        if (listToDisplay == null) {
            return new ComboBoxData();
        }

        Pattern pattern = Pattern.compile("\\{\\w+\\}");
        Matcher matcher = pattern.matcher(widgetConfig.getPatternConfig().getValue());
        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();
        for (DomainObject domainObject : listToDisplay) {
            String format = format(domainObject, matcher);
            idDisplayMapping.put(domainObject.getId(), format);

        }

        ReferenceValue fieldPathValue = formData.getFieldPathValue(getFieldPath(widgetConfig));
        Id selectedId = fieldPathValue == null ? null : fieldPathValue.get();

        ComboBoxData result = new ComboBoxData();
        result.setListValues(idDisplayMapping);
        result.setId(selectedId);

        return result;
    }

    private String format(DomainObject domainObject, Matcher matcher) {
        StringBuffer replacement = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            String fieldName = group.substring(1, group.length() - 1);
            matcher.appendReplacement(replacement, domainObject.getValue(fieldName).toString());
        }
        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }
}
