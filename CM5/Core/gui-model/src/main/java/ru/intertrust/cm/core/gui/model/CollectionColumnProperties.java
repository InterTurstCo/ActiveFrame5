package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.collection.view.AscSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.collection.view.DescSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

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
    public static final String DATE_PATTERN = "datePattern";
    public static final String TIME_PATTERN = "timePattern";
    public static final String TIME_ZONE_ID = "timeZoneId";
    public static final String NAME_KEY = "name";
    public static final String WIDTH = "width";
    public static final String MIN_WIDTH = "minWidth";
    public static final String MAX_WIDTH = "maxWidth";
    public static final String RESIZABLE = "resizable";
    public static final String TEXT_BREAK_STYLE = "textBreakStyle";
    public static final String SORTABLE = "sortable";
    public static final String SORTED_MARKER = "sorted";
    public static final String FIELD_NAME = "field";
    public static final String INITIAL_FILTER_VALUES = "initialFilterValues";
    public static final String DATE_RANGE = "range-date";
    public static final String HIDDEN = "hidden";
    public static final String CHILD_COLLECTIONS_CONFIG = "childCollectionsConfig";
    public static final String DRILL_DOWN_STYLE = "textBreakStyle";
    public static final String EXPANDABLE= "expandable";

    private AscSortCriteriaConfig ascSortCriteriaConfig;
    private DescSortCriteriaConfig descSortCriteriaConfig;
    private HashMap<String, Object> properties = new HashMap<String, Object>();
    private RendererConfig rendererConfig;
    private ImageMappingsConfig imageMappingsConfig;
    private ActionRefConfig actionRefConfig;
    private ActionContext actionContext;

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

    public RendererConfig getRendererConfig() {
        return rendererConfig;
    }

    public void setRendererConfig(RendererConfig rendererConfig) {
        this.rendererConfig = rendererConfig;
    }

    public ImageMappingsConfig getImageMappingsConfig() {
        return imageMappingsConfig;
    }

    public void setImageMappingsConfig(ImageMappingsConfig imageMappingsConfig) {
        this.imageMappingsConfig = imageMappingsConfig;
    }

    public CollectionColumnProperties addProperty(final String name, final Object value) {
        if (value != null) {
            properties.put(name, value);
        } else {
            properties.remove(name);
        }
        return this;
    }

    public ActionRefConfig getActionRefConfig() {
        return actionRefConfig;
    }

    public void setActionRefConfig(ActionRefConfig actionRefConfig) {
        this.actionRefConfig = actionRefConfig;
    }


    public Object getProperty(final String name) {
        return properties.get(name);
    }

    public HashMap<String, Object> getProperties() {
        return new HashMap<String, Object>(properties);
    }

    public ActionContext getActionContext() {
        return actionContext;
    }

    public void setActionContext(ActionContext actionContext) {
        this.actionContext = actionContext;
    }
}
