package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ComboBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("combo-box")
public class ComboBoxHandler extends SingleObjectWidgetHandler {
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected ConfigurationService configurationService;

    @Override
    public ComboBoxState getInitialState(WidgetContext context) {
        ComboBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        String field = fieldPath.getFieldName();
        String objectType = context.getFormObjects().getNode(fieldPath.getParentPath()).getType();
        ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) configurationService.getFieldConfig(objectType, field);
        // todo: find LINKED: only cities of that country
        // List<DomainObject> listToDisplay = getCrudService().findLinkedDomainObjects(rootObjectForComboBoxField.getId(), "city", "country");
        List<DomainObject> listToDisplay = crudService.findAll(fieldConfig.getType());
        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();
        idDisplayMapping.put(null, "");

        ComboBoxState result = new ComboBoxState();
        result.setListValues(idDisplayMapping);

        if (listToDisplay == null) {
            return result;
        }

        appendDisplayMappings(listToDisplay, widgetConfig.getPatternConfig().getValue(), idDisplayMapping);

        Id selectedId = context.getFieldPlainValue();

        result.setId(selectedId);

        return result;
    }

    @Override
    public Value getValue(WidgetState state) {
        return new ReferenceValue(((ComboBoxState) state).getId());
    }
}
