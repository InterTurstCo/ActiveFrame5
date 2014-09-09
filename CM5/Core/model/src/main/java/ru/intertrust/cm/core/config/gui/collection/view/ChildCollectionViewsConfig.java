package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 09.09.14
 *         Time: 18:08
 */

@Root(name="child-collection-views")
public class ChildCollectionViewsConfig implements Dto {

    @Attribute(name="link-field")
    private String linkField;

    @ElementList(inline = true, required = false)
    private List<ChildCollectionViewerConfig> childCollectionViewerConfigList = new ArrayList<>();

    public String getLinkField() {
        return linkField;
    }

    public List<ChildCollectionViewerConfig> getChildCollectionViewerConfigList() {
        return childCollectionViewerConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChildCollectionViewsConfig that = (ChildCollectionViewsConfig) o;
        if (!childCollectionViewerConfigList.equals(that.childCollectionViewerConfigList)) {
            return false;
        }
        if (!linkField.equals(that.linkField)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = linkField.hashCode();
        result = 31 * result + childCollectionViewerConfigList.hashCode();
        return result;
    }
}
