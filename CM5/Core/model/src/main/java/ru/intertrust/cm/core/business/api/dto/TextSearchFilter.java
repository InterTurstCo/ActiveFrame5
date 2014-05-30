package ru.intertrust.cm.core.business.api.dto;

public class TextSearchFilter extends SearchFilterBase {

    private String text;

    public TextSearchFilter() {
    }

    public TextSearchFilter(String fieldName) {
        super(fieldName);
    }

    public TextSearchFilter(String fieldName, String text) {
        super(fieldName);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "contains [" + text + "]";
    }
}
