package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class RepresentationResponse implements Dto {
    private Id id;
    private String representation;

    public RepresentationResponse() {
    }

    public RepresentationResponse(String representation) {
        this.representation = representation;
    }

    public RepresentationResponse(Id id, String representation) {
        this.id = id;
        this.representation = representation;
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

}
