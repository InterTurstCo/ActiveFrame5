package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Panel;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 09.12.13
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
public class ResizeTreeAnimation extends Animation {
    private int startWidth = 0;
    private int desiredWidth = 0;
    private Panel resizedWidget;
    boolean expanded;


    public ResizeTreeAnimation(int desiredWidth, Panel resizedWidget) {
        this.resizedWidget = resizedWidget;
        this.startWidth = resizedWidget.getOffsetWidth();
        this.desiredWidth = desiredWidth;
        expanded = startWidth < desiredWidth;
    }

    @Override
    protected void onUpdate(double progress) {
        double width = extractProportionalLength(progress);
        resizedWidget.setWidth(width+ Style.Unit.PX.getType());
        if (width == desiredWidth) {
            String styleName = expanded ? "navigation-dynamic-panel-expanded" : "navigation-dynamic-panel";
            resizedWidget.setStyleName(styleName);
        }
    }

    private double extractProportionalLength(double progress) {
        double outWidth = startWidth - (startWidth - desiredWidth) * progress;
        return outWidth;
    }

    public Panel getResizedWidget() {
        return resizedWidget;
    }


}