package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07.08.2014
 *         Time: 1:24
 */
public class SimpleTextTooltip extends PopupPanel {
    public SimpleTextTooltip(String text) {
        super(true);
        init(text);
    }

    private void init(String text) {
        Label textLabel = new Label(text);
        textLabel.setStyleName("tooltipText");
        this.add(textLabel);
        this.setStyleName("tooltipPopup");
        final com.google.gwt.user.client.Timer styleTimer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                SimpleTextTooltip.this.getElement().replaceClassName("tooltipPopup", "tooltipPopupHidden");
                this.cancel();
            }
        };
        styleTimer.scheduleRepeating(1000);
        final com.google.gwt.user.client.Timer closeTimer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                SimpleTextTooltip.this.hide();
                this.cancel();
            }
        };
        closeTimer.scheduleRepeating(5000);
    }
}