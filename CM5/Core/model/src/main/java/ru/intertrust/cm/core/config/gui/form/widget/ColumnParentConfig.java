package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
public class ColumnParentConfig implements Dto {
    @Attribute(name = "header")
    @Localizable
    private String header;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ColumnParentConfig that = (ColumnParentConfig) o;

        if (header != null ? !header.equals(that.header) : that.header != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return header != null ? header.hashCode() : 0;
    }
}
