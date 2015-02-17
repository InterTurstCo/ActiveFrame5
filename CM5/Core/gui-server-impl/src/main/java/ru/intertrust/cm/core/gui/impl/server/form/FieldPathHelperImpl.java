package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.gui.api.server.form.FieldPathHelper;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

/**
 * @author Denis Mitavskiy
 *         Date: 17.02.2015
 *         Time: 15:18
 */
public class FieldPathHelperImpl implements FieldPathHelper {
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public String getReferencedObjectType(String rootObjectType, FieldPath path) {
        if (path.isOneToManyReference()) {
            return path.getLinkedObjectType();
        } else if (path.isManyToManyReference()) {
            final String linkToChildrenName = path.getLinkToChildrenName();
            final String linkingObjectType = path.getLinkingObjectType();
            return ((ReferenceFieldConfig) configurationExplorer.getFieldConfig(linkingObjectType, linkToChildrenName)).getType();
        } else { // one-to-one reference
            return ((ReferenceFieldConfig)configurationExplorer.getFieldConfig(rootObjectType, path.getFieldName())).getType();
        }
    }

    public boolean typeMatchesFieldPath(String type, String formRootObjectType, FieldPath path, boolean exactMatch) {
        if (exactMatch) {
            return type.equalsIgnoreCase(getReferencedObjectType(formRootObjectType, path));
        } else {
            return configurationExplorer.isInstanceOf(type, getReferencedObjectType(formRootObjectType, path));
        }
    }
}
