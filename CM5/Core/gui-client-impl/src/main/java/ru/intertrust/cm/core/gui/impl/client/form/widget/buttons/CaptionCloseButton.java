package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 12.12.2014
 *         Time: 8:46
 */
public class CaptionCloseButton extends AbsolutePanel {
    public CaptionCloseButton() {
        this.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().deleteBtn());
    }
    public void addClickListener(EventListener listener){
        DOM.setEventListener(getElement(), listener);
        DOM.sinkEvents(getElement(), Event.ONCLICK);
    }

}
