package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;

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

    private HashMap<String, Object> properties = new HashMap<String, Object>();

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
