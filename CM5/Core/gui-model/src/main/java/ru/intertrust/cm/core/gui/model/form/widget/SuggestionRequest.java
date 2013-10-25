package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 24.10.13
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class SuggestionRequest implements Dto {

    private String text;
    private String collectionName;
    private String pattern;
    private ArrayList<Long> excludeIds = new ArrayList<Long>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public ArrayList<Long> getExcludeIds() {
        return excludeIds;
    }

    public void setExcludeIds(ArrayList<Long> excludeIds) {
        this.excludeIds = excludeIds;
    }
}
