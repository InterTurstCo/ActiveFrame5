package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.PanelResizeListener;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.12.2014
 *         Time: 8:30
 */
public class HierarchyBrowserElementResizeListener implements PanelResizeListener {
    private Widget widget;
    private Double widthFactor;
    private Double heightFactor;


    public HierarchyBrowserElementResizeListener(Widget widget, Double widthFactor, Double heightFactor) {
        this.widget = widget;
        this.widthFactor = widthFactor;
        this.heightFactor = heightFactor;
    }

    @Override
    public void onPanelResize(int width, int height) {
        if(widthFactor != null){
            widget.setWidth(widthFactor * width + "px");
        }
        if(heightFactor != null){
            widget.setHeight(heightFactor * height + "px");
        }
    }

}
