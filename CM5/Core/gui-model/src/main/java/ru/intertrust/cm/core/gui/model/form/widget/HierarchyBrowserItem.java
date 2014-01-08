package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserItem implements Dto {
    private String stringRepresentation;
    private Id id;
    private String nodeCollectionName;
    private boolean chosen;

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

    public String getNodeCollectionName() {
        return nodeCollectionName;
    }

    public void setNodeCollectionName(String nodeCollectionName) {
        this.nodeCollectionName = nodeCollectionName;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HierarchyBrowserItem that = (HierarchyBrowserItem) o;

        if (chosen != that.chosen) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (nodeCollectionName != null ? !nodeCollectionName.equals(that.nodeCollectionName) : that.
                nodeCollectionName != null) {
            return false;
        }
        if (stringRepresentation != null ? !stringRepresentation.equals(that.stringRepresentation) : that.
                stringRepresentation != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = stringRepresentation != null ? stringRepresentation.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (nodeCollectionName != null ? nodeCollectionName.hashCode() : 0);
        result = 31 * result + (chosen ? 1 : 0);
        return result;
    }
}
