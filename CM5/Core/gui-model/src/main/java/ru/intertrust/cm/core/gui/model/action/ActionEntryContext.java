package ru.intertrust.cm.core.gui.model.action;

import java.util.HashMap;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.util.ComponentAttributeHelper;

/**
 * @author Sergey.Okolot
 *         Created on 16.04.2014 16:00.
 */
public class ActionEntryContext implements Dto {

    private HashMap<String, Object> attributes = new HashMap<String, Object>();

    public void setAttribute(final String name, final Object value) {
        if (value != null) {
            attributes.put(name, value);
        }
    }

    public <T> T getAttribute(final String name) {
        return (T) attributes.get(name);
    }

    public String getId() {
        return getAttribute(ComponentAttributeHelper.ID_ATTR);
    }

    public String getComponentName() {
        return getAttribute(ComponentAttributeHelper.COMPONENT_NAME_ATTR);
    }
}
