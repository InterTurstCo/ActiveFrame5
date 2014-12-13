package ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.CaptionCloseButton;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 14:33
 */
public class DialogBoxUtils {
    public static void addCaptionCloseButton(final DialogBox dialogBox){
        HTML caption = (HTML) dialogBox.getCaption();
        CaptionCloseButton captionCloseButton = new CaptionCloseButton();
        captionCloseButton.addClickListener(new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                dialogBox.hide();
            }
        });

        caption.getElement().appendChild(captionCloseButton.getElement());
    }
}
