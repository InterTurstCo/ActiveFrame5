package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.gui.model.form.FieldPath;

/**
 * @author Denis Mitavskiy
 *         Date: 17.02.2015
 *         Time: 15:16
 */
public interface FieldPathHelper {
    String getReferencedObjectType(String rootObjectType, FieldPath path);

    boolean typeMatchesFieldPath(String type, String formRootObjectType, FieldPath path, boolean exactMatch);
}
