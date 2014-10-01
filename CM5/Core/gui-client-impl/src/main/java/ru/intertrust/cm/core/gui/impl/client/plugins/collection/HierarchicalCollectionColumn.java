package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.HierarchicalCell;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class HierarchicalCollectionColumn extends TextCollectionColumn {

    private final List<ChildCollectionViewerConfig> childCollectionViewerConfigs;
    private final EventBus eventBus;

    public HierarchicalCollectionColumn(HierarchicalCell cell, String fieldName, Boolean resizable, ValueConverter converter,
                                        List<ChildCollectionViewerConfig> childCollectionViewerConfigs, EventBus eventBus) {
        super(cell, fieldName, resizable, converter);
        this.childCollectionViewerConfigs = childCollectionViewerConfigs;
        this.eventBus = eventBus;
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element target, CollectionRowItem rowItem, NativeEvent event) {
        if ("click".equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            Element element = Element.as(eventTarget);
            if ("expand-arrow".equals(element.getClassName())) {
                eventBus.fireEvent(new HierarchicalCollectionEvent(rowItem.getId(), childCollectionViewerConfigs));
            }
        }
    }

}

