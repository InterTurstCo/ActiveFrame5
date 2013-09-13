package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 18:29
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "field-paths")
public class FieldPathsConfig implements Dto{
    @ElementList(inline = true)
    private List<FieldPathConfig> rows = new ArrayList<>();

    public List<FieldPathConfig> getRows() {
        return rows;
    }

    public void setRows(List<FieldPathConfig> rows) {
        this.rows = rows;
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

        if (rows != null ? !rows.equals(that.rows) : that.rows != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return rows != null ? rows.hashCode() : 0;
    }
}
