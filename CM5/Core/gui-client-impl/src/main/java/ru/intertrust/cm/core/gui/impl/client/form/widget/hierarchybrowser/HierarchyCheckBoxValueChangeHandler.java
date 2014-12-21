package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCheckBoxUpdateEvent;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.12.2014
 *         Time: 7:23
 */
public class HierarchyCheckBoxValueChangeHandler implements ValueChangeHandler<Boolean> {
    private HierarchyBrowserItem item;
    private EventBus eventBus;

    public HierarchyCheckBoxValueChangeHandler(HierarchyBrowserItem item, EventBus eventBus) {
        this.item = item;
        this.eventBus = eventBus;
    }

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean chosen = event.getValue();
        item.setChosen(chosen);
        eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(item));
    }

    public HierarchyBrowserItem getItem() {
        return item;
    }

}