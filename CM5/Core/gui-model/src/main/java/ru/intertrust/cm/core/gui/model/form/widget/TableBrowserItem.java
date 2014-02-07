package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.11.13
 *         Time: 16:15
 */
public class TableBrowserItem implements Dto {

    private Id id;
    private String stringRepresentation;

    public TableBrowserItem() {
    }

    public TableBrowserItem(Id id, String stringRepresentation) {
        this.id = id;
        this.stringRepresentation = stringRepresentation;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }


    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public void setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableBrowserItem that = (TableBrowserItem) o;

        if (id != null ? !id.equals(that.id) :
                that.id != null) {
            return false;
        }

        if (stringRepresentation != null ? !stringRepresentation.equals(that.stringRepresentation) :
                that.stringRepresentation != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (stringRepresentation != null ? stringRepresentation.hashCode() : 0);
        return result;
    }
}
