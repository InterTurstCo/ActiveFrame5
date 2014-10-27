package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Panel;

/**
 * @author Lesia Puhova
 *         Date: 16.10.14
 *         Time: 14:32
 */
class TextPresenter implements AttachmentElementPresenter {

    private String text;
    private ClickHandler clickHandler;

    TextPresenter(String text) {
        this.text = text;
    }

    TextPresenter(String text, ClickHandler clickHandler) {
        this.text = text;
        this.clickHandler = clickHandler;
    }

    @Override
    public Panel presentElement() {
        Panel element = new AbsolutePanel();

        element.setStyleName("facebook-element");
        String anchorTitle = text;
        Anchor fileNameAnchor = new Anchor(anchorTitle);
        if (clickHandler != null) {
            fileNameAnchor.addClickHandler(clickHandler);
        }
        element.add(fileNameAnchor);

        return element;
    }
}
