package ru.intertrust.cm.core.gui.impl.client.form.widget.suggestbox;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.10.2014
 *         Time: 10:21
 */
public class SuggestTextBox extends TextBox {

    /*
     * gwt suggest-box doesn't handle combo of keys,
      * so "shift+M" will be handled
     * like two events with the same letter,
     * suggest-box do nothing if previous
     * filter letter equal current
     */
    @Override
    public void onBrowserEvent(Event event) {
        if(isSkippedKeyEvent(event)){
            return;
        }
        super.onBrowserEvent(event);

    }
    private boolean isSkippedKeyEvent(Event event){
        return  event.getTypeInt() == KeyCodes.KEY_FIRST_MEDIA_KEY
                ||(event.getTypeInt() == Event.ONKEYUP && (event.getKeyCode() == KeyCodes.KEY_DOWN
                || event.getKeyCode() == KeyCodes.KEY_UP));
    }

}
