package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleSelectionWidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxState;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("combo-box")
public class ComboBoxHandler extends ListWidgetHandler {

    @Autowired
    protected CrudService crudService;

    @Override
    public ComboBoxState getInitialState(WidgetContext context) {
        ComboBoxState widgetState = new ComboBoxState();
        setupInitialState(widgetState, context);

        return widgetState;
    }

    private void setupInitialState(ComboBoxState widgetState, WidgetContext context) {
        SingleSelectionWidgetConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();
        idDisplayMapping.put(null, "");
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
                widgetState.setOriginalObjects(listToDisplay);
            }
        }
    }
}
