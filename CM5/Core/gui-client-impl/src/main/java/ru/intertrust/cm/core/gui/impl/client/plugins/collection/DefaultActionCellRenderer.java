package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.AbstractCell;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ActionCollectionColumn;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 04.04.2016
 * Time: 12:37
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("default.action.cell.renderer")
public class DefaultActionCellRenderer implements Component {
    public static final String ACT_CELL_RENDERER_COMPONENT = "default.action.cell.renderer";
    public CollectionColumn getActionColumn(AbstractCell cell, String fieldName, EventBus eventBus, boolean resizable, ValueConverter converter, ActionContext actionContext){
        return new ActionCollectionColumn(cell, fieldName, eventBus,resizable, converter, actionContext);
    }

    @Override
    public String getName() {
        return ACT_CELL_RENDERER_COMPONENT;
    }

    @Override
    public Component createNew() {
        return new DefaultActionCellRenderer();
    }
}
