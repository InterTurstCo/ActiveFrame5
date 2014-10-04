package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public abstract class AbstractNoneEditablePanel extends Composite {
    protected AbsolutePanel mainBoxPanel;
    protected AbsolutePanel container;
    protected Style.Display displayStyle;

    public AbstractNoneEditablePanel(SelectionStyleConfig selectionStyleConfig) {
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        mainBoxPanel.getElement().getStyle().setDisplay(displayStyle);
        container = new AbsolutePanel();
        container.add(mainBoxPanel);
        initWidget(container);

    }

}
