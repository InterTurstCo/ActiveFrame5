package ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.01.2015
 *         Time: 18:54
 */
public class LinkEditingNavigationHandler implements KeyDownHandler, BlurHandler {

    private WidgetDelegatedKeyDownHandler widgetDelegatedKeyDownHandler;

    public void handleNavigation(Widget widget, WidgetDelegatedKeyDownHandler widgetDelegatedKeyDownHandler){
        this.widgetDelegatedKeyDownHandler = widgetDelegatedKeyDownHandler;
        widget.addDomHandler(this, KeyDownEvent.getType());
        widget.addDomHandler(this, BlurEvent.getType());
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        widgetDelegatedKeyDownHandler.preHandle();
        if (widgetDelegatedKeyDownHandler.shouldHandle()) {
            int eventKeyCode = event.getNativeEvent().getKeyCode();
            switch (eventKeyCode) {
                case KeyCodes.KEY_BACKSPACE:
                case KeyCodes.KEY_DELETE:
                    widgetDelegatedKeyDownHandler.handleBackspaceOrDeleteDown();
                    break;
                case KeyCodes.KEY_ESCAPE:
                    widgetDelegatedKeyDownHandler.resetUserInteraction();
                    break;
                case KeyCodes.KEY_LEFT:
                    widgetDelegatedKeyDownHandler.highlightNextLeftItem();
                    break;
                case KeyCodes.KEY_RIGHT:
                    widgetDelegatedKeyDownHandler.highlightNextRightItem();
                    break;
                default:
                    widgetDelegatedKeyDownHandler.defaultBehaviour();
            }
        }

    }

    @Override
    public void onBlur(BlurEvent event) {
        widgetDelegatedKeyDownHandler.resetUserInteraction();
    }
}
