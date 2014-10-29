package ru.intertrust.cm.core.config.gui.navigation.calendar;

import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.PatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;

/**
 * @author Sergey.Okolot
 *         Created on 28.10.2014 13:16.
 */
public class CalendarViewConfig implements Dto {

    @Element(name = "collection-ref")
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "date-field-path")
    private FieldPathConfig dateFieldPath;

    @Element(name = "date-field-filter")
    private DateFieldFilterConfig dateFieldFilterConfig;

    @Element(name = "pattern")
    private PatternConfig pattern;

    @Element(name = "sort-criteria", required = false)
    private SortCriteriaConfig sortCriteriaConfig;

    @Element(name = "renderer", required = false)
    private RendererConfig rendererConfig;

    @Element(name = "image-field", required = false)
    private ImageFieldConfig imageFieldConfig;

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public FieldPathConfig getDateFieldPath() {
        return dateFieldPath;
    }

    public DateFieldFilterConfig getDateFieldFilterConfig() {
        return dateFieldFilterConfig;
    }

    public PatternConfig getPattern() {
        return pattern;
    }

    public SortCriteriaConfig getSortCriteriaConfig() {
        return sortCriteriaConfig;
    }

    public RendererConfig getRendererConfig() {
        return rendererConfig;
    }

    public ImageFieldConfig getImageFieldConfig() {
        return imageFieldConfig;
    }
}
