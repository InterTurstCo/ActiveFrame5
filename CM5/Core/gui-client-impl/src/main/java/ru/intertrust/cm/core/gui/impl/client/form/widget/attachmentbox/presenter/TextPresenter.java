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
public class TextPresenter implements AttachmentElementPresenter {

    private final String text;
    private final ClickHandler clickHandler;

    public TextPresenter(String text) {
        this.text = text;
        clickHandler = null;
    }

    public TextPresenter(String text, ClickHandler clickHandler) {
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
