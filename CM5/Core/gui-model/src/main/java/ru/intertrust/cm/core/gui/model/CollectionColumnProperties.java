package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.collection.view.AscSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.collection.view.DescSortCriteriaConfig;

import java.util.HashMap;

/**
 * @author Sergey.Okolot
 */
public class CollectionColumnProperties implements Dto {
    /**
     * @defaultUID
     */
    private static final long serialVersionUID = 1L;

    public static final String TYPE_KEY = "type";
    public static final String SEARCH_FILTER_KEY = "searchFilter";
    public static final String PATTERN_KEY = "pattern";
    public static final String NAME_KEY = "name";
    public static final String MIN_WIDTH = "min-width";
    public static final String MAX_WIDTH = "max-width";
    public static final String RESIZABLE = "resizable";
    public static final String TEXT_BREAK_STYLE = "text-break-style";
    public static final String SORTABLE = "sortable";
    public static final String SORTED_MARKER = "sorted";
    public static final String FIELD_NAME = "field";
    private AscSortCriteriaConfig ascSortCriteriaConfig;
    private DescSortCriteriaConfig descSortCriteriaConfig;
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public AscSortCriteriaConfig getAscSortCriteriaConfig() {
        return ascSortCriteriaConfig;
    }

    public void setAscSortCriteriaConfig(AscSortCriteriaConfig ascSortCriteriaConfig) {
        this.ascSortCriteriaConfig = ascSortCriteriaConfig;
    }

    public DescSortCriteriaConfig getDescSortCriteriaConfig() {
        return descSortCriteriaConfig;
    }

    public void setDescSortCriteriaConfig(DescSortCriteriaConfig descSortCriteriaConfig) {
        this.descSortCriteriaConfig = descSortCriteriaConfig;
    }

    public CollectionColumnProperties addProperty(final String name, final Object value) {
        if (value != null) {
            properties.put(name, value);
        } else {
            properties.remove(name);
        }
        return this;
    }

    public Object getProperty(final String name) {
        return properties.get(name);
    }

    public HashMap<String, Object> getProperties() {
        return new HashMap<String, Object>(properties);
    }
}
