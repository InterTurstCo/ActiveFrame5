package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.event.collection.RedrawCollectionRowEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ControlExpandableCell;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.CollectionDataGridUtils;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.06.2015
 *         Time: 8:52
 */
public class ExpandableColumn extends CollectionParameterizedColumn {

    private ValueConverter converter;
    private EventBus eventBus;
    public ExpandableColumn(ControlExpandableCell cell) {
        super(cell);

    }

    @Override
    public String getValue(CollectionRowItem object) {
        return converter.valueToString(object.getRowValue(fieldName));

    }

    public ExpandableColumn(ControlExpandableCell cell, String fieldName, Boolean resizable, ValueConverter converter, EventBus eventBus) {
        super(cell, fieldName, resizable);
        this.converter = converter;
        this.eventBus = eventBus;
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element target, CollectionRowItem rowItem, NativeEvent event) {
        if ("click".equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            Element element = Element.as(eventTarget);
            String id = element.getClassName().replaceAll("expandSign", BusinessUniverseConstants.EMPTY_VALUE)
                    .replaceAll("collapseSign", BusinessUniverseConstants.EMPTY_VALUE);
            CollectionRowItem effectedRowItem = CollectionDataGridUtils.getEffectedItem(rowItem, id);
            if (element.getClassName().startsWith("expandSign")) {
                eventBus.fireEvent(new RedrawCollectionRowEvent(rowItem, effectedRowItem, true));
            }else if(element.getClassName().startsWith("collapseSign")){
                eventBus.fireEvent(new RedrawCollectionRowEvent(rowItem, effectedRowItem,false));
            }
        }
        makeEventHandled(event);

    }

    private void makeEventHandled(NativeEvent event){
        event.stopPropagation();
        event.preventDefault();
    }
}
