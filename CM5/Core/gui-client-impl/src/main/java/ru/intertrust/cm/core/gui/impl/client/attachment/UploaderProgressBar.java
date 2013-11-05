package ru.intertrust.cm.core.gui.impl.client.attachment;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

public final class UploaderProgressBar extends HorizontalPanel {

    private static final double COMPLETE_PERECENTAGE = 100d;
    private static final double START_PERECENTAGE = 0d;
    private Panel progress;
    int progressBarWidth = 0;

    public UploaderProgressBar(int progressBarWidth) {
        this.progressBarWidth = progressBarWidth;

        progress = new SimplePanel();
        applyStylesProgress();
        applyStylesForThisObject();
        this.add(progress);
    }

    public void update(final int percentage) {
        if (percentage < START_PERECENTAGE || percentage > COMPLETE_PERECENTAGE) {
            throw new IllegalArgumentException("invalid value for percentage");
        }

        int progressWidth = (int) (progressBarWidth / COMPLETE_PERECENTAGE * percentage);

        progress.setWidth(progressWidth + "px");
    }

    private void applyStylesProgress() {
        Style style = progress.getElement().getStyle();
        style.setHeight(10, Style.Unit.PX);
        style.setBackgroundColor("blue");
        style.setFontSize(0, Style.Unit.PX);
        style.setWidth(0, Style.Unit.PX);

    }

    private void applyStylesForThisObject() {
        Style style = this.getElement().getStyle();
        style.setBorderWidth(1, Style.Unit.PX);
        style.setBackgroundColor("lightgrey");
        style.setMarginRight(5, Style.Unit.PX);
        style.setMarginLeft(5, Style.Unit.PX);
        style.setWidth(progressBarWidth, Style.Unit.PX);
        this.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    }
}