package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 17:50
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "column")
public class ColumnConfig implements Dto {
    @Attribute(name = "header")
    private String header;

    @Element(name = "column")
    private ColumnConfig columnConfig;

    @Element(name = "formatting", required = false)
    private FormattingConfig formattingConfig;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ColumnConfig getColumnConfig() {
        return columnConfig;
    }

    public void setColumnConfig(ColumnConfig columnConfig) {
        this.columnConfig = columnConfig;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ColumnConfig that = (ColumnConfig) o;

        if (columnConfig != null ? !columnConfig.equals(that.columnConfig) : that.columnConfig != null) {
            return false;
        }
        if (formattingConfig != null ? !formattingConfig.equals(that.formattingConfig) : that.formattingConfig != null) {
            return false;
        }
        if (header != null ? !header.equals(that.header) : that.header != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = header != null ? header.hashCode() : 0;
        result = 31 * result + (columnConfig != null ? columnConfig.hashCode() : 0);
        result = result + (formattingConfig != null ? formattingConfig.hashCode() : 0);
        return result;
    }
}
