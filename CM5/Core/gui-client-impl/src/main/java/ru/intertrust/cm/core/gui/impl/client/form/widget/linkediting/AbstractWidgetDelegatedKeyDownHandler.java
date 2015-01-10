package ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting;

import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.WidgetNavigator;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.01.2015
 *         Time: 9:05
 */
public abstract class AbstractWidgetDelegatedKeyDownHandler<T extends Widget> implements WidgetDelegatedKeyDownHandler {
    private static final String DEFAULT_ORDINAL_STYLE_CLASS_NAME = "facebook-element";
    private static final String DEFAULT_HIGHLIGHTED_STYLE_CLASS_NAME = "highlightedFacebookElement";
    protected EventBus eventBus;
    protected WidgetNavigator<T> widgetNavigator;

    public AbstractWidgetDelegatedKeyDownHandler(WidgetNavigator<T> widgetNavigator, EventBus eventBus) {
        this.widgetNavigator = widgetNavigator;
        this.eventBus = eventBus;
    }
    public void resetUserInteraction() {
        changeHighlighting(false);
        widgetNavigator.reset();

    }

    protected void changeHighlighting(boolean wasHighlighted) {
        Widget currentSelectedItem = widgetNavigator.getCurrent();
        if (currentSelectedItem != null) {
            String styleName = wasHighlighted ? getHighlightedStyleClassName() : getOrdinalStyleClassName();
            currentSelectedItem.setStyleName(styleName);
        }

    }

    public void highlightNextRightItem(){
        widgetNavigator.forward();
        highlightNextItem();

    }
    public void highlightNextLeftItem(){
        widgetNavigator.back();
        highlightNextItem();

    }

    private void highlightNextItem() {
        Widget previousItem = widgetNavigator.getPrevious();
        if (previousItem != null) {
            previousItem.setStyleName(getOrdinalStyleClassName());
        }

        Widget itemToBeHighlighted = widgetNavigator.getCurrent();
        if (itemToBeHighlighted != null) {
            itemToBeHighlighted.setStyleName(getHighlightedStyleClassName());
        }

    }
    public void preHandle(){
        //nothing to do most of the cases
    }

    public boolean shouldHandle(){
        return true;
    }

    public void defaultBehaviour() {
        changeHighlighting(false);
    }

    protected String getOrdinalStyleClassName() {
        return DEFAULT_ORDINAL_STYLE_CLASS_NAME;
    }

    protected String getHighlightedStyleClassName() {
        return DEFAULT_HIGHLIGHTED_STYLE_CLASS_NAME;
    }
}
