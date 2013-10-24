package ru.intertrust.cm.core.gui.impl.client.attachment;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

public final class UploaderProgressBar extends SimplePanel {

    private static final double COMPLETE_PERECENTAGE = 100d;
    private static final double START_PERECENTAGE = 0d;
    private Panel progress;
    int width = 0;
    public UploaderProgressBar(int width) {
        this.width = width;

        applyStylesForThisObject();
        progress = new SimplePanel();
        applyStylesProgress();

        add(progress);

    }

    public void update(final int percentage) {
        if (percentage < START_PERECENTAGE || percentage > COMPLETE_PERECENTAGE) {
            throw new IllegalArgumentException("invalid value for percentage");
        }

        int decorationWidth = getAbsoluteLeft() - getAbsoluteLeft();

        int barWidth = this.getOffsetWidth();
        int progressWidth = (int) (((barWidth - (decorationWidth * 2)) / COMPLETE_PERECENTAGE) * percentage);

        progress.setWidth(progressWidth + "px");
    }
    private  void applyStylesProgress(){
        Style style =  progress.getElement().getStyle();
        style.setHeight(10 , Style.Unit.PX);
      //  style.setVerticalAlign(Style.VerticalAlign.MIDDLE);
        style.setBackgroundColor("blue");

      //  style.setMarginRight(2, Style.Unit.PX);
      //  style.setMarginLeft(2, Style.Unit.PX);
        style.setFontSize(0, Style.Unit.PX);
        style.setWidth(0, Style.Unit.PX);
    }

    private void applyStylesForThisObject() {
      Style style =  this.getElement().getStyle();
        style.setBorderWidth(1, Style.Unit.PX);
        style.setBackgroundColor("lightgrey");
       // style.setVerticalAlign(Style.VerticalAlign.MIDDLE);
        style.setMarginRight(5, Style.Unit.PX);
        style.setMarginLeft(5, Style.Unit.PX);
        style.setWidth(width, Style.Unit.PX);

    }
}