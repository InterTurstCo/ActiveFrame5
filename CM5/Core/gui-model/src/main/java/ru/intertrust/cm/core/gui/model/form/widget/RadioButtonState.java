package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * @author Lesia Puhova
 *         Date: 16.01.2014
 *         Time: 11:42:27
 */

public class RadioButtonState extends SingleSelectionWidgetState {

    public enum Layout {
        VERTICAL,
        HORIZONTAL
    }

    private Layout layout;

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Layout getLayout() {
        return layout;
    }

}
