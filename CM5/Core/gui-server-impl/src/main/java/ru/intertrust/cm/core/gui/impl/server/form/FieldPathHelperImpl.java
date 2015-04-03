package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.gui.api.server.form.FieldPathHelper;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.util.Iterator;

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
            return ((ReferenceFieldConfig) configurationExplorer.getFieldConfig(rootObjectType, path.getFieldName())).getType();
        }
    }

    public boolean typeMatchesFieldPath(String type, String formRootObjectType, FieldPath path, boolean exactMatch) {
        if (exactMatch) {
            return type.equalsIgnoreCase(getReferencedObjectType(formRootObjectType, path));
        } else {
            return configurationExplorer.isAssignable(type, getReferencedObjectType(formRootObjectType, path));
        }
    }

    public boolean isDirectReference(String rootObjectType, FieldPath path) {
        if (path.isOneToOneReference()) {
            return true;
        }
        final Iterator<FieldPath.Element> elementIterator = path.elementsIterator();
        String domainObjectType = rootObjectType;
        while (elementIterator.hasNext()) {
            final FieldPath.Element elt = elementIterator.next();
            if (elt instanceof FieldPath.OneToOneDirectReference) {
                domainObjectType = ((ReferenceFieldConfig) configurationExplorer.getFieldConfig(domainObjectType, elt.getName())).getType();
            } else if (elt instanceof FieldPath.OneToOneBackReference) {
                domainObjectType = ((FieldPath.OneToOneBackReference) elt).getLinkedObjectType();
            } else if (elt instanceof FieldPath.Field) { // last element, no more else, it's not possible
                return configurationExplorer.getFieldConfig(domainObjectType, elt.getName()) instanceof ReferenceFieldConfig;
            } else { // multi back reference
                return false;
            }
        }
        return false;
    }
}
