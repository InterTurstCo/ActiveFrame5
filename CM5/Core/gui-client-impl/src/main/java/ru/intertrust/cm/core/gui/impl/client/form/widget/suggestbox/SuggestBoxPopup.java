package ru.intertrust.cm.core.gui.impl.client.form.widget.suggestbox;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 31.08.2014
 *         Time: 16:10
 */
public class SuggestBoxPopup extends DecoratedPopupPanel {
    public SuggestBoxPopup(boolean autoHide, boolean modal) {
        super(autoHide, modal);

    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    hide();
                }
                break;
        }
    }
}
