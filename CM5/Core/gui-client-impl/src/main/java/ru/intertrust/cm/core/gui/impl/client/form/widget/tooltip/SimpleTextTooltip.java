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
        this.setStyleName("tooltip-popup");
        final com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                SimpleTextTooltip.this.hide();
                this.cancel();
            }
        };
        timer.scheduleRepeating(2000);
    }
}