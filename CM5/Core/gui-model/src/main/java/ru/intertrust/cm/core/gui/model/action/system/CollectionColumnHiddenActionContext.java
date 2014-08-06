package ru.intertrust.cm.core.gui.model.action.system;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sergey.Okolot
 *         Created on 06.08.2014 11:46.
 */
public class CollectionColumnHiddenActionContext extends AbstractUserSettingActionContext {

    public static final String COMPONENT_NAME = "collection.column.hidden.action";

    private Set<String> hiddenFields = new HashSet<>();

    public CollectionColumnHiddenActionContext putHidden(final String fieldName) {
        hiddenFields.add(fieldName);
        return this;
    }

    public Collection<String> getHiddenFields() {
        return hiddenFields;
    }
}
