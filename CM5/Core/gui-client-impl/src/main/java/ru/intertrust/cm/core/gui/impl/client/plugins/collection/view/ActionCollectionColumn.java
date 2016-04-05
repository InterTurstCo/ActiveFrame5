package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.SimpleServerAction;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionParameterizedColumn;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 04.04.2016
 * Time: 15:39
 * To change this template use File | Settings | File and Code Templates.
 */
public class ActionCollectionColumn extends CollectionParameterizedColumn {
    private ValueConverter converter;
    private ActionContext actionContext;
    private String actionComponentName;
    public ActionCollectionColumn(AbstractCell cell) {
        super(cell);
    }

    @Override
    public String getValue(CollectionRowItem object) {
        return converter.valueToString(object.getRowValue(fieldName));

    }

    public ActionCollectionColumn(AbstractCell cell, String fieldName, EventBus eventBus, boolean resizable, ValueConverter converter,ActionContext actionContext) {
        super(cell, fieldName, eventBus, resizable);
        this.converter = converter;
        this.actionContext = actionContext;
        this.actionComponentName = ((ActionConfig)actionContext.getActionConfig()).getComponentName();
    }

    @Override
    protected void performAction(Cell.Context context){
        CollectionRowItem item = (CollectionRowItem)context.getKey();
        actionContext.setRootObjectId(item.getId());
        CollectionAction action = new CollectionAction();
        action.setInitialContext(actionContext);
        action.perform();
    }

    class CollectionAction extends SimpleServerAction {
        @Override
        public Component createNew() {
            return new CollectionAction();
        }

        @Override
        public String getName(){
            return actionComponentName;
        }
    }
}
