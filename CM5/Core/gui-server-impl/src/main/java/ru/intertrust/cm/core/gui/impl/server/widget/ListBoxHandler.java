package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.ListBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.10.13
 *         Time: 16:25
 */
@ComponentName("list-box")
public class ListBoxHandler extends LinkEditingWidgetHandler {
    @Autowired
    protected CrudService crudService;

    @Override
    public ListBoxState getInitialState(WidgetContext context) {
        ListBoxConfig widgetConfig = context.getWidgetConfig();

        String linkType = getLinkedObjectType(context, context.getFieldPath());

        List<DomainObject> listToDisplay = crudService.findAll(linkType);
        LinkedHashMap<Id, String> idDisplayMapping = new LinkedHashMap<>();

        ListBoxState result = new ListBoxState();
        result.setListValues(idDisplayMapping);

        if (listToDisplay == null) {
            return result;
        }

        String displayPattern = widgetConfig.getPatternConfig().getValue();
        appendDisplayMappings(listToDisplay, displayPattern, idDisplayMapping);

        ArrayList<Id> selectedIds = context.getObjectIds();
        result.setSelectedIds(selectedIds);

        return result;
    }

}
