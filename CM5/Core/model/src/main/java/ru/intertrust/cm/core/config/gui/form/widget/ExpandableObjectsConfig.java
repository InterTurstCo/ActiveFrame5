package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 18.09.2015
 */
@Root(name = "expandable-objects")
public class ExpandableObjectsConfig  implements Dto {

    @ElementList(entry="expandable-object", type=ExpandableObjectConfig.class, inline=true, required = true)
    private List<ExpandableObjectConfig> expandableObjects = new ArrayList<ExpandableObjectConfig>();

    public List<ExpandableObjectConfig> getExpandableObjects() {
        return expandableObjects;
    }

    public void setExpandableObjects(List<ExpandableObjectConfig> expandableObjects) {
        this.expandableObjects = expandableObjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExpandableObjectsConfig that = (ExpandableObjectsConfig) o;

        if (expandableObjects != null ? !expandableObjects.equals(that.expandableObjects) :
                that.expandableObjects != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return expandableObjects != null ? expandableObjects.hashCode() : 0;
    }
}
