package ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting;


import com.google.gwt.user.client.ui.Widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.01.2015
 *         Time: 9:01
 */
public interface WidgetDelegatedKeyDownHandler<T extends Widget> {
    void handleBackspaceOrDeleteDown();
    void resetUserInteraction();
    void highlightNextRightItem();
    void highlightNextLeftItem();
    void defaultBehaviour();
    void preHandle();
    boolean shouldHandle();
}
