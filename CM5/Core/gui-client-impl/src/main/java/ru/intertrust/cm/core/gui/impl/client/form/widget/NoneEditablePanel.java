package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.01.14
 *         Time: 13:15
 */
public abstract class NoneEditablePanel extends Composite {
    private static final String DISPLAY_STYLE_TABLE = "table";
    protected AbsolutePanel mainBoxPanel;
    protected Style.Display displayStyle;

    public NoneEditablePanel() {
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");
        initWidget(mainBoxPanel);

    }

    protected void displayItem(final String itemRepresentation) {

        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label-none-editable");
        label.getElement().getStyle().setDisplay(displayStyle);
        mainBoxPanel.add(label);
    }

    protected void initDisplayStyle(String howToDisplay) {
        if (DISPLAY_STYLE_TABLE.equalsIgnoreCase(howToDisplay)) {
            displayStyle = Style.Display.BLOCK;

        } else {
            displayStyle = Style.Display.INLINE_BLOCK;
        }

    }

    public abstract void showSelectedItems(String howToDisplay);


}
