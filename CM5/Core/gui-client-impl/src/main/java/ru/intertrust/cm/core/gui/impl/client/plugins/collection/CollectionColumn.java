package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionAddElementEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionAddGroupEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionRowFilteredEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionRowStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.Arrays;

/**
 * @author Denis Mitavskiy
 *         Date: 21.01.14
 *         Time: 22:59
 */
public abstract class CollectionColumn<T> extends Column<CollectionRowItem, T> {

    protected String fieldName;
    protected boolean resizable = true;
    private int userWidth;
    protected int minWidth;
    protected int maxWidth = BusinessUniverseConstants.MAX_COLUMN_WIDTH;
    protected boolean moveable = true;
    protected boolean visible;
    protected int drawWidth;
    protected EventBus eventBus;


    public CollectionColumn(AbstractCell cell) {
        super(cell);
    }

    public CollectionColumn(AbstractCell cell, String fieldName, EventBus eventBus, boolean resizable) {
        super(cell);
        this.fieldName = fieldName;
        this.eventBus = eventBus;
        this.resizable = resizable;

    }


    public String getFieldName() {
        return fieldName;
    }

    public Boolean isResizable() {
        return resizable;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setResizable(Boolean resizable) {
        this.resizable = resizable;
    }

    public int getUserWidth() {
        return userWidth;
    }

    public void setUserWidth(int userWidth) {
        this.userWidth = userWidth;
    }

    public int getMinWidth() {
        return minWidth == 0 ? BusinessUniverseConstants.MIN_RESIZE_COLUMN_WIDTH : minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public boolean isMoveable() {
        return moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getDrawWidth() {
        return drawWidth;
    }

    public void setDrawWidth(int drawWidth) {
        this.drawWidth = drawWidth;
    }


    @Override
    public void onBrowserEvent(Cell.Context context, Element target, ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem rowItem, NativeEvent event) {
        String type = event.getType();
        int keyCode = event.getKeyCode();
        EventTarget eventTarget = event.getEventTarget();
        Element element = Element.as(eventTarget);
        if (BrowserEvents.CLICK.equals(type)) {
            if (element.getClassName().startsWith("expandSign")) {
                eventBus.fireEvent(new CollectionRowStateChangedEvent(rowItem,true));
            }else if(element.getClassName().startsWith("collapseSign")){
                eventBus.fireEvent(new CollectionRowStateChangedEvent(rowItem, false));
            }
            else if(element.getClassName().startsWith("createElement")){
                eventBus.fireEvent(new CollectionAddElementEvent(rowItem, false));
            }
            else if(element.getClassName().startsWith("createGroup")){
                eventBus.fireEvent(new CollectionAddGroupEvent(rowItem, false));
            }
            else if(element.getClassName().startsWith("actionCollectionColumn")){
                performAction(context);
            }
            else {
                super.onBrowserEvent(context, target, rowItem, event);
            }
        }else if(BrowserEvents.KEYDOWN.equalsIgnoreCase(type) && KeyCodes.KEY_ENTER == keyCode && element.getClassName()
                .startsWith("hierarchicalFilterInput")){
            String text = getInputElement(element).getValue();
            rowItem.putFilterValues("name", Arrays.asList(text));
            eventBus.fireEvent(new CollectionRowFilteredEvent(rowItem));
        }
        else {
            super.onBrowserEvent(context, target, rowItem, event);
        }

    }
    private InputElement getInputElement(Element parent) {
        return parent.<InputElement> cast();
    }

    protected void performAction(Cell.Context context){}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectionColumn)) {
            return false;
        }

        CollectionColumn that = (CollectionColumn) o;

        if (maxWidth != that.maxWidth) {
            return false;
        }

        if (minWidth != that.minWidth) {
            return false;
        }
        if (moveable != that.moveable) {
            return false;
        }
        if (resizable != that.resizable) {
            return false;
        }
        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (resizable ? 1 : 0);
        result = 31 * result + minWidth;
        result = 31 * result + maxWidth;
        result = 31 * result + (moveable ? 1 : 0);
        return result;
    }
}
