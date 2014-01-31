package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 *
 * @author atsvetkov
 *
 */
public class CollectionColumnConfig implements Dto {

    @Attribute(required = true)
    private String field;

    @Attribute(required = true)
    private String name;

    @Attribute(required = false)
    private boolean hidden;

    @Attribute(required = false)
    private boolean sortable;

    @Attribute(required = false)
    private boolean editable;

    @Attribute(required = true)
    private String type;

    @Attribute(name = "pattern", required = false)
    private String pattern;

    @Attribute(name = "time-zone-id", required = false)
    private String timeZoneId;

    @Attribute(name = "search-filter", required = false)
    private String searchFilter;

    @Element(name = "asc-sort-criteria", required = false)
    private AscSortCriteriaConfig ascSortCriteriaConfig;

    @Element(name = "desc-sort-criteria", required = false)
    private DescSortCriteriaConfig descSortCriteriaConfig;

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

    public String getPattern() {
        return pattern;
    }

    public String getTimeZoneId() {
        return timeZoneId;
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

    public DescSortCriteriaConfig getDescSortCriteriaConfig() {
        return descSortCriteriaConfig;
    }

    public void setDescSortCriteriaConfig(DescSortCriteriaConfig descSortCriteriaConfig) {
        this.descSortCriteriaConfig = descSortCriteriaConfig;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionColumnConfig that = (CollectionColumnConfig) o;

        if (editable != that.editable) {
            return false;
        }
        if (hidden != that.hidden) {
            return false;
        }
        if (sortable != that.sortable) {
            return false;
        }
        if (ascSortCriteriaConfig != null ? !ascSortCriteriaConfig.equals(that.ascSortCriteriaConfig) :
                that.ascSortCriteriaConfig != null) {
            return false;
        }
        if (descSortCriteriaConfig != null ? !descSortCriteriaConfig.equals(that.descSortCriteriaConfig) :
                that.descSortCriteriaConfig != null) {
            return false;
        }
        if (field != null ? !field.equals(that.field) : that.field != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) {
            return false;
        }
        if (searchFilter != null ? !searchFilter.equals(that.searchFilter) :
                that.searchFilter != null) {
            return false;
        }
        if (timeZoneId != null ? !timeZoneId.equals(that.timeZoneId) : that.timeZoneId != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (sortable ? 1 : 0);
        result = 31 * result + (editable ? 1 : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        result = 31 * result + (timeZoneId != null ? timeZoneId.hashCode() : 0);
        result = 31 * result + (searchFilter != null ? searchFilter.hashCode() : 0);
        result = 31 * result + (ascSortCriteriaConfig != null ? ascSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (descSortCriteriaConfig != null ? descSortCriteriaConfig.hashCode() : 0);
        return result;
    }
}
