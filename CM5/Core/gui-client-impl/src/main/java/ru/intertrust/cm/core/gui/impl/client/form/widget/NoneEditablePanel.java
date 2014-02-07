package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.01.14
 *         Time: 13:15
 */
public  class NoneEditablePanel extends AbstractNoneEditablePanel {
    public NoneEditablePanel(SelectionStyleConfig selectionStyleConfig) {
        super(selectionStyleConfig);
    }

    public void displayItem(final String itemRepresentation) {
        AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setDisplay(displayStyle);
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label-none-editable");
        element.add(label);
        mainBoxPanel.add(element);
    }

}
