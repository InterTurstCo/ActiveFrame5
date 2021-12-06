package ru.intertrust.cm.core.gui.model.form.widget;

/**
 * Created by Myskin Sergey on 11.01.2021.
 */
public class ColorPickerState extends WidgetState {

    public ColorPickerState() {
    }

    public ColorPickerState(String hexCode) {
        this.hexCode = hexCode;
    }

    private String hexCode;

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ColorPickerState that = (ColorPickerState) o;

        return hexCode != null ? hexCode.equals(that.hexCode) : that.hexCode == null;
    }

}
