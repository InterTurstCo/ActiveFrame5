package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FocusPanel;

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
    private FocusPanel resizedWidget;


    public ResizeTreeAnimation(int desiredWidth, FocusPanel resizedWidget) {
        this.resizedWidget = resizedWidget;
        this.startWidth = resizedWidget.getOffsetWidth();
        this.desiredWidth = desiredWidth;
    }

    @Override
    protected void onUpdate(double progress) {
        double width = extractProportionalLength(progress);
        resizedWidget.setWidth(width+ Style.Unit.PX.getType());

    }

    private double extractProportionalLength(double progress) {
        double outWidth = startWidth - (startWidth - desiredWidth) * progress;
        return outWidth;
    }

    public FocusPanel getResizedWidget() {
        return resizedWidget;
    }


}