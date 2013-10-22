package ru.intertrust.cm.core.gui.impl.markup.uploader;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 18.10.13
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
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
      //  setStyleName("ProgressBar");
        applyStylesForThisObject();

        progress = new SimplePanel();
       // progress.setStyleName("progress");
        applyStylesProgress();
        progress.setWidth("0px");

        add(progress);
        //setWidth();
    }

    public void update(final int percentage) {
        if (percentage < START_PERECENTAGE || percentage > COMPLETE_PERECENTAGE) {
            throw new IllegalArgumentException("invalid value for percentage");
        }

        int decorationWidth = progress.getAbsoluteLeft() - getAbsoluteLeft();

        int barWidth = this.getOffsetWidth();
        int progressWidth = (int) (((barWidth - (decorationWidth * 2)) / COMPLETE_PERECENTAGE) * percentage);

        progress.setWidth(progressWidth + "px");
    }
    private  void applyStylesProgress(){
      Style style =  progress.getElement().getStyle();
        style.setHeight(15 , Style.Unit.PX);
        style.setBackgroundColor("blue");
        style.setMarginRight(2, Style.Unit.PX);
        style.setMarginLeft(2, Style.Unit.PX);
        style.setFontSize(0, Style.Unit.PX);
    }

    private void applyStylesForThisObject() {
      Style style =  this.getElement().getStyle();
        style.setBorderWidth(1, Style.Unit.PX);
        style.setBackgroundColor("red");

        style.setMarginRight(5, Style.Unit.PX);
        style.setMarginLeft(5, Style.Unit.PX);
       style.setWidth(width, Style.Unit.PX);


    }
}