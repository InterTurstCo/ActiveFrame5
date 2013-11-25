package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "field-paths")
public class FieldPathsConfig implements Dto{
    @ElementList(inline = true)
    private List<FieldPathConfig> fieldPathConfigsList = new ArrayList<FieldPathConfig>();

    public List<FieldPathConfig> getFieldPathConfigsList() {
        return fieldPathConfigsList;
    }

    public void setFieldPathConfigsList(List<FieldPathConfig> fieldPathConfigsList) {
        this.fieldPathConfigsList = fieldPathConfigsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldPathsConfig that = (FieldPathsConfig) o;

        if (fieldPathConfigsList != null ? !fieldPathConfigsList.equals(that.fieldPathConfigsList) : that.
                fieldPathConfigsList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return fieldPathConfigsList != null ? fieldPathConfigsList.hashCode() : 0;
    }
}
