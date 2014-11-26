package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.11.2014
 *         Time: 18:19
 */
public abstract class TableBrowserEditableComposite extends Composite {
    protected TextBox filter;
    protected AbsolutePanel root;
    protected Button openTooltip;
    public abstract void clearContent();
    public String getFilterValue(){
        return filter.getValue();
    }

    public void removeTooltipButton(){
        if(openTooltip != null){
        openTooltip.removeFromParent();
        }
    }
}
