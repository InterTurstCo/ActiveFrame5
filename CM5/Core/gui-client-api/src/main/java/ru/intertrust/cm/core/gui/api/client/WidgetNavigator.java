package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.01.2015
 *         Time: 9:01
 */
public interface WidgetNavigator<T extends Widget> {
    void forward();
    void back();
    void reset();
    T getCurrent();
    T getPrevious();
}
