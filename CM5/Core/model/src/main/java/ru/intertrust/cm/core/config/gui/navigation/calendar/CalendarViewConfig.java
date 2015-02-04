package ru.intertrust.cm.core.config.gui.navigation.calendar;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;

import java.util.List;

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

    @Element(name = "sort-criteria", required = false)
    private SortCriteriaConfig sortCriteriaConfig;

    @Element(name = "renderer", required = false)
    private RendererConfig rendererConfig;

    @Element(name = "month-item", required = false)
    private CalendarItemConfig monthItemConfig;

    @ElementList(name = "day-items", required = false)
    private List<CalendarItemConfig> dayItemsConfig;

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

    public SortCriteriaConfig getSortCriteriaConfig() {
        return sortCriteriaConfig;
    }

    public RendererConfig getRendererConfig() {
        return rendererConfig;
    }

    public CalendarItemConfig getMonthItemConfig() {
        return monthItemConfig;
    }

    public List<CalendarItemConfig> getDayItemsConfig() {
        return dayItemsConfig;
    }

    public ImageFieldConfig getImageFieldConfig() {
        return imageFieldConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarViewConfig that = (CalendarViewConfig) o;
        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) : that
                .collectionRefConfig != null) {
            return false;
        }
        if (dateFieldFilterConfig != null ? !dateFieldFilterConfig.equals(that.dateFieldFilterConfig) : that
                .dateFieldFilterConfig != null) {
            return false;
        }
        if (dateFieldPath != null ? !dateFieldPath.equals(that.dateFieldPath) : that.dateFieldPath != null) {
            return false;
        }
        if (dayItemsConfig != null ? !dayItemsConfig.equals(that.dayItemsConfig) : that.dayItemsConfig != null) {
            return false;
        }
        if (imageFieldConfig != null ? !imageFieldConfig.equals(that.imageFieldConfig) : that.imageFieldConfig !=
                null) {
            return false;
        }
        if (monthItemConfig != null ? !monthItemConfig.equals(that.monthItemConfig) : that.monthItemConfig != null) {
            return false;
        }
        if (rendererConfig != null ? !rendererConfig.equals(that.rendererConfig) : that.rendererConfig != null) {
            return false;
        }
        if (sortCriteriaConfig != null ? !sortCriteriaConfig.equals(that.sortCriteriaConfig) : that
                .sortCriteriaConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionRefConfig != null ? collectionRefConfig.hashCode() : 0;
        result = 31 * result + (dateFieldPath != null ? dateFieldPath.hashCode() : 0);
        result = 31 * result + (dateFieldFilterConfig != null ? dateFieldFilterConfig.hashCode() : 0);
        result = 31 * result + (sortCriteriaConfig != null ? sortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (rendererConfig != null ? rendererConfig.hashCode() : 0);
        result = 31 * result + (monthItemConfig != null ? monthItemConfig.hashCode() : 0);
        result = 31 * result + (dayItemsConfig != null ? dayItemsConfig.hashCode() : 0);
        result = 31 * result + (imageFieldConfig != null ? imageFieldConfig.hashCode() : 0);
        return result;
    }
}
