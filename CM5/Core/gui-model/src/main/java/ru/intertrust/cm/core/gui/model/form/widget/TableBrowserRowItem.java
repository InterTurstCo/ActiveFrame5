package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.11.13
 *         Time: 16:15
 */
public class TableBrowserRowItem implements Dto {

    private Id id;
    private String selectedRowRepresentation;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }


    public String getSelectedRowRepresentation() {
        return selectedRowRepresentation;
    }

    public void setSelectedRowRepresentation(String selectedRowRepresentation) {
        this.selectedRowRepresentation = selectedRowRepresentation;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableBrowserRowItem that = (TableBrowserRowItem) o;

        if (id != null ? !id.equals(that.id) :
                that.id != null) {
            return false;
        }

        if (selectedRowRepresentation != null ? !selectedRowRepresentation.equals(that.selectedRowRepresentation) :
                that.selectedRowRepresentation != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (selectedRowRepresentation != null ? selectedRowRepresentation.hashCode() : 0);
        return result;
    }
}
