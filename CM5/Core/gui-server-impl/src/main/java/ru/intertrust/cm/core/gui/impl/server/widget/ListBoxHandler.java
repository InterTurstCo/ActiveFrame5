package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.ListBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.10.13
 *         Time: 16:25
 */
@ComponentName("list-box")
public class ListBoxHandler extends ListWidgetHandler {
    @Autowired
    protected CrudService crudService;

    @Override
    public ListBoxState getInitialState(WidgetContext context) {
        ListBoxConfig widgetConfig = context.getWidgetConfig();

        final FieldPath[] fieldPaths = context.getFieldPaths();
        String[] linkTypes = getLinkedObjectTypes(context, fieldPaths);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig) ;
        List<DomainObject> domainObjectsToDisplay = new ArrayList<>();
        HashMap<Id, Integer> idFieldPathIndexMapping = new HashMap<>();
        for (int i = 0; i < linkTypes.length; i++) {
            String linkType = linkTypes[i];
            List<DomainObject> domainObjects = crudService.findAll(linkType);
            if (domainObjects != null) {
                domainObjectsToDisplay.addAll(domainObjects);
                for (DomainObject domainObject : domainObjects) {
                    idFieldPathIndexMapping.put(domainObject.getId(), i);
                }
            }
        }
        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();
        if (singleChoice) {
            idDisplayMapping.put(null, "");
        }

        ListBoxState result = new ListBoxState();
        result.setFieldPaths(fieldPaths);
        result.setListValues(idDisplayMapping);
        result.setIdFieldPathIndexMapping(idFieldPathIndexMapping);
        result.setSingleChoice(singleChoice);
        if (domainObjectsToDisplay.isEmpty()) {
            return result;
        }

        String displayPattern = widgetConfig.getPatternConfig().getValue();
        appendDisplayMappings(domainObjectsToDisplay, displayPattern, idDisplayMapping);

        ArrayList<ArrayList<Id>> allIds = context.getObjectIds();
        result.setSelectedIds(allIds);
        result.setOriginalObjects(domainObjectsToDisplay);
        return result;
    }

}
