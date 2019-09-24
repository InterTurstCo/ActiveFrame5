package ru.intertrust.cm.core.gui.model.form.widget;


/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 13:41
 */
public class LabelState extends WidgetState {
    private String label;
    private boolean asteriskRequired;
    private String pattern;
    private String fontWeight;
    private String fontStyle;
    private String fontSize;
    private String relatedWidgetId;
    private String textDecoration;
    private String backgroundColor;

    public LabelState() {
    }

    public LabelState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isAsteriskRequired() {
        return asteriskRequired;
    }

    public void setAsteriskRequired(boolean asteriskRequired) {
        this.asteriskRequired = asteriskRequired;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(String textDecoration) {
        this.textDecoration = textDecoration;
    }

    @Override
    // modified to return true always, so to make isEditable() method of LabelWidget
    // to indicate whether containing panel is editable or not.
    public boolean isEditable() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String getRelatedWidgetId() {
        return relatedWidgetId;
    }

    public void setRelatedWidgetId(String relatedWidgetId) {
        this.relatedWidgetId = relatedWidgetId;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
