package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 17.07.2014
 *         Time: 13:01
 */
@Root(name = "form-objects-remover")
public class FormObjectsRemoverConfig implements Dto {
    @Attribute(name = "handler", required = false)
    private String handler;

    @Element(name = "on-delete", required = false)
    private OnDeleteConfig onDelete;

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public OnDeleteConfig getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(OnDeleteConfig onDelete) {
        this.onDelete = onDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormObjectsRemoverConfig that = (FormObjectsRemoverConfig) o;

        if (handler != null ? !handler.equals(that.handler) : that.handler != null) {
            return false;
        }
        if (onDelete != null ? !onDelete.equals(that.onDelete) : that.onDelete != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return handler != null ? handler.hashCode() : 0;
    }
}
