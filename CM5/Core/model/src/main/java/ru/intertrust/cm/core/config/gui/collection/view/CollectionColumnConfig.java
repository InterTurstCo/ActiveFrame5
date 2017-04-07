package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author atsvetkov
 *
 */
@Root(name = "column")
public class CollectionColumnConfig implements Dto {

    @Attribute(name = "field", required = true)
    private String field;

    @Attribute(name = "name", required = true)
    @Localizable
    private String name;

    @Attribute(name = "hidden", required = false)
    private boolean hidden;

    @Attribute(name = "sortable",required = false)
    private boolean sortable;

    @Attribute(name = "editable",required = false)
    private boolean editable;

    @Attribute(name = "type", required = true)
    private String type;

    @Attribute(name = "date-pattern", required = false)
    private String datePattern;

    @Attribute(name = "time-pattern", required = false)
    private String timePattern;

    @Attribute(name = "time-zone-id", required = false)
    private String timeZoneId;

    @Attribute(name = "search-filter", required = false)
    private String searchFilter;

    @Attribute(name = "width", required = false)
    private String width;

    @Attribute(name = "min-width", required = false)
    private String minWidth;

    @Attribute(name = "max-width", required = false)
    private String maxWidth;

    @Attribute(name = "resizable", required = false)
    private boolean resizable = true;

    @Attribute(name = "text-break-style", required = false)
    private String textBreakStyle;

    @Attribute(name = "date-range", required = false)
    private boolean dateRange;

    @Attribute(name ="drill-down-style", required = false)
    private String drillDownStyle;

    @Attribute(name="expandable", required = false)
    private boolean expandable;

    @Element(name = "asc-sort-criteria", required = false)
    private AscSortCriteriaConfig ascSortCriteriaConfig;

    @Element(name = "desc-sort-criteria", required = false)
    private DescSortCriteriaConfig descSortCriteriaConfig;

    @Element(name = "image-mappings", required = false)
    private ImageMappingsConfig imageMappingsConfig;

    @Element(name = "renderer", required = false)
    private RendererConfig rendererConfig;

    @ElementList(inline = true, required = false)
    private List<ChildCollectionViewerConfig> childCollectionViewerConfigList = new ArrayList<>();

    @Element(name = "action-ref", required = false)
    private ActionRefConfig actionRefConfig;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatePattern() {
        return datePattern == null ? ModelUtil.DEFAULT_DATE_PATTERN : datePattern;
    }

    public String getTimeZoneId() {
        return timeZoneId == null ? ModelUtil.DEFAULT_TIME_ZONE_ID : timeZoneId;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public AscSortCriteriaConfig getAscSortCriteriaConfig() {
        return ascSortCriteriaConfig;
    }

    public void setAscSortCriteriaConfig(AscSortCriteriaConfig ascSortCriteriaConfig) {
        this.ascSortCriteriaConfig = ascSortCriteriaConfig;
    }

    public ActionRefConfig getActionRefConfig() {
        return actionRefConfig;
    }

    public void setActionRefConfig(ActionRefConfig actionRefConfig) {
        this.actionRefConfig = actionRefConfig;
    }

    public DescSortCriteriaConfig getDescSortCriteriaConfig() {
        return descSortCriteriaConfig;
    }

    public void setDescSortCriteriaConfig(DescSortCriteriaConfig descSortCriteriaConfig) {
        this.descSortCriteriaConfig = descSortCriteriaConfig;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(String minWidth) {
        this.minWidth = minWidth;
    }

    public String getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(String maxWidth) {
        this.maxWidth = maxWidth;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public String getTextBreakStyle() {
        return textBreakStyle;
    }

    public void setTextBreakStyle(String textBreakStyle) {
        this.textBreakStyle = textBreakStyle;
    }

    public String getDrillDownStyle() {
        return drillDownStyle;
    }

    public ImageMappingsConfig getImageMappingsConfig() {
        return imageMappingsConfig;
    }

    public void setImageMappingsConfig(ImageMappingsConfig imageMappingsConfig) {
        this.imageMappingsConfig = imageMappingsConfig;
    }

    public RendererConfig getRendererConfig() {
        return rendererConfig;
    }

    public void setRendererConfig(RendererConfig rendererConfig) {
        this.rendererConfig = rendererConfig;
    }

    public String getTimePattern() {
        return timePattern;
    }

    public void setTimePattern(String timePattern) {
        this.timePattern = timePattern;
    }

    public boolean isDateRange() {
        return dateRange;
    }

    public void setDateRange(boolean dateRange) {
        this.dateRange = dateRange;
    }


    public List<ChildCollectionViewerConfig> getChildCollectionViewerConfigList() {
        return childCollectionViewerConfigList;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionColumnConfig that = (CollectionColumnConfig) o;

        if (hidden != that.hidden) return false;
        if (sortable != that.sortable) return false;
        if (editable != that.editable) return false;
        if (resizable != that.resizable) return false;
        if (dateRange != that.dateRange) return false;
        if (expandable != that.expandable) return false;
        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (datePattern != null ? !datePattern.equals(that.datePattern) : that.datePattern != null) return false;
        if (timePattern != null ? !timePattern.equals(that.timePattern) : that.timePattern != null) return false;
        if (timeZoneId != null ? !timeZoneId.equals(that.timeZoneId) : that.timeZoneId != null) return false;
        if (searchFilter != null ? !searchFilter.equals(that.searchFilter) : that.searchFilter != null) return false;
        if (width != null ? !width.equals(that.width) : that.width != null) return false;
        if (minWidth != null ? !minWidth.equals(that.minWidth) : that.minWidth != null) return false;
        if (maxWidth != null ? !maxWidth.equals(that.maxWidth) : that.maxWidth != null) return false;
        if (textBreakStyle != null ? !textBreakStyle.equals(that.textBreakStyle) : that.textBreakStyle != null)
            return false;
        if (drillDownStyle != null ? !drillDownStyle.equals(that.drillDownStyle) : that.drillDownStyle != null)
            return false;
        if (ascSortCriteriaConfig != null ? !ascSortCriteriaConfig.equals(that.ascSortCriteriaConfig) : that.ascSortCriteriaConfig != null)
            return false;
        if (descSortCriteriaConfig != null ? !descSortCriteriaConfig.equals(that.descSortCriteriaConfig) : that.descSortCriteriaConfig != null)
            return false;
        if (imageMappingsConfig != null ? !imageMappingsConfig.equals(that.imageMappingsConfig) : that.imageMappingsConfig != null)
            return false;
        if (rendererConfig != null ? !rendererConfig.equals(that.rendererConfig) : that.rendererConfig != null)
            return false;
        if (childCollectionViewerConfigList != null ? !childCollectionViewerConfigList.equals(that.childCollectionViewerConfigList) : that.childCollectionViewerConfigList != null)
            return false;
        if (actionRefConfig != null ? !actionRefConfig.equals(that.actionRefConfig) : that.actionRefConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
