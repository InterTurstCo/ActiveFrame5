package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleSelectionWidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.SingleSelectionWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 14.01.14
 *         Time: 18:29
 *
 *  Обработчик для виджета, позволяющего выбирать одно значение из нескольких возможных.
 *  Является супер-классом для конкретных обработчиков, таких как ComboBoxHandler и RadioButtonHandler.
 */
public abstract class SingleSelectionWidgetHandler extends SingleObjectWidgetHandler {
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected ConfigurationService configurationService;

    protected void setupInitialState(SingleSelectionWidgetState widgetState, WidgetContext context) {
        SingleSelectionWidgetConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        String field = fieldPath.getFieldName();
        String objectType = context.getFormObjects().getNode(fieldPath.getParentPath()).getType();
        ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) configurationService.getFieldConfig(objectType, field);
        // todo: find LINKED: only cities of that country
        // List<DomainObject> listToDisplay = getCrudService().findLinkedDomainObjects(rootObjectForComboBoxField.getId(), "city", "country");
        List<DomainObject> listToDisplay = crudService.findAll(fieldConfig.getType());
        Map<Id, String> idDisplayMapping = new LinkedHashMap<>();
        widgetState.setListValues(idDisplayMapping);

        if (listToDisplay != null) {
            appendDisplayMappings(listToDisplay, widgetConfig.getPatternConfig().getValue(), idDisplayMapping);
            Id selectedId = context.getFieldPlainValue();
            widgetState.setId(selectedId);
        }
    }

    @Override
    public Value getValue(WidgetState state) {
        return new ReferenceValue(((SingleSelectionWidgetState) state).getId());
    }
}
