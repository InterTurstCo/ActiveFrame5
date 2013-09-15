package ru.intertrust.cm.core.config.model.gui.navigation;

import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@SuppressWarnings("serial")
@Root(name = "custom")
public class CustomPluginConfig extends PluginConfigParent {

    @Attribute(name = "name")
    private String name;

    @ElementList(inline = true, required = false)
    private List<AttributeConfig> attributeConfigList = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AttributeConfig> getAttributeConfigList() {
        return attributeConfigList;
    }

    public void setAttributeConfigList(List<AttributeConfig> attributeConfigList) {
        this.attributeConfigList = attributeConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomPluginConfig that = (CustomPluginConfig) o;

        if (attributeConfigList != null ? !attributeConfigList.equals(that.getAttributeConfigList()) : that.
                getAttributeConfigList() != null) {
                    return false;
        }

        if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = attributeConfigList != null ? attributeConfigList.hashCode() : 0;
        result = 23 * result + name != null ? name.hashCode() : 0;
        return result;
    }
}

