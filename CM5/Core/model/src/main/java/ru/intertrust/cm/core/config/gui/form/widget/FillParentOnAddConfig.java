package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.05.14
 *         Time: 13:15
 */
@Root(name = "fill-parent-on-add")
public class FillParentOnAddConfig implements Dto {

    @Attribute(name = "field-to-fill", required = true)
    private String fieldToFill;

    @Attribute(name = "collection", required = false)
    private String collection;

    @Attribute(name = "collection-field", required = false)
    private String collectionField;

    @Attribute(name = "filter-by-parent-id", required = false)
    private String filterByParentId;

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getCollectionField() {
        return collectionField;
    }

    public void setCollectionField(String collectionField) {
        this.collectionField = collectionField;
    }

    public String getFieldToFill() {
        return fieldToFill;
    }

    public void setFieldToFill(String fieldToFill) {
        this.fieldToFill = fieldToFill;
    }

    public String getFilterByParentId() {
        return filterByParentId;
    }

    public void setFilterByParentId(String filterByParentId) {
        this.filterByParentId = filterByParentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FillParentOnAddConfig that = (FillParentOnAddConfig) o;

        if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
            return false;
        }
        if (collectionField != null ? !collectionField.equals(that.collectionField) : that.collectionField != null) {
            return false;
        }
        if (fieldToFill != null ? !fieldToFill.equals(that.fieldToFill) : that.fieldToFill != null) {
            return false;
        }
        if (filterByParentId != null ? !filterByParentId.equals(that.filterByParentId) : that.filterByParentId != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldToFill != null ? fieldToFill.hashCode() : 0;
        result = 31 * result + (collection != null ? collection.hashCode() : 0);
        result = 31 * result + (collectionField != null ? collectionField.hashCode() : 0);
        result = 31 * result + (filterByParentId != null ? filterByParentId.hashCode() : 0);
        return result;
    }
}
