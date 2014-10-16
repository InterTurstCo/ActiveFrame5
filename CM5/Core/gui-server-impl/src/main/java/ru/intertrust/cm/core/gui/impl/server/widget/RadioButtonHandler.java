package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RadioButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleSelectionWidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.RadioButtonState;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 14.01.14
 *         Time: 18:29
 */
@ComponentName("radio-button")
public class RadioButtonHandler extends ListWidgetHandler {

    @Autowired
    protected CrudService crudService;
    @Autowired
    protected ConfigurationService configurationService;

    @Override
    public RadioButtonState getInitialState(WidgetContext context) {
        RadioButtonState widgetState = new RadioButtonState();
        setupInitialState(widgetState, context);

        RadioButtonConfig radioButtonConfig = context.getWidgetConfig();
        if (radioButtonConfig.getLayoutConfig() != null && "horizontal".equalsIgnoreCase(radioButtonConfig.getLayoutConfig().getName())) {
            widgetState.setLayout(RadioButtonState.Layout.HORIZONTAL);
        } else {
            widgetState.setLayout(RadioButtonState.Layout.VERTICAL);
        }

        return widgetState;
    }

    private void setupInitialState(RadioButtonState widgetState, WidgetContext context) {
        SingleSelectionWidgetConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();
        widgetState.setListValues(idDisplayMapping);
        String field = fieldPath.getFieldName();
        String objectType = context.getFormObjects().getNode(fieldPath.getParentPath()).getType();
        ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) configurationService.getFieldConfig(objectType, field);
        // todo: find LINKED: only cities of that country
        // List<DomainObject> listToDisplay = getCrudService().findLinkedDomainObjects(rootObjectForComboBoxField.getSelectedId(), "city", "country");
        if (fieldConfig != null) {
            List<DomainObject> listToDisplay = crudService.findAll(fieldConfig.getType());
            if (listToDisplay != null) {
                appendDisplayMappings(listToDisplay, widgetConfig.getPatternConfig().getValue(), idDisplayMapping);
                Id selectedId = context.getFieldPlainValue();
                widgetState.setSelectedId(selectedId);
            }
        }
    }
}
