package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
public class HyperlinkItem implements Dto {
    private Id id;
    private String representation;

    public HyperlinkItem(Id id, String representation) {
        this.id = id;
        this.representation = representation;
    }

    public HyperlinkItem() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HyperlinkItem that = (HyperlinkItem) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (representation != null ? !representation.equals(that.representation) : that.representation != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (representation != null ? representation.hashCode() : 0);
        return result;
    }
}
