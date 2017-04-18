package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IPetrov on 06.03.14.
 */
@Root(name = "navigation-panel-mappings")
public class NavigationPanelMappingsConfig implements Dto, TopLevelConfig {
    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @ElementList(inline = true)
    private List<NavigationPanelMappingConfig> navigationPanelMappingConfigs = new ArrayList<NavigationPanelMappingConfig>();

    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NavigationPanelMappingConfig> getNavigationPanelMappingConfigList() {
        return navigationPanelMappingConfigs;
    }

    public void setNavigationPanelMappingConfigList(List<NavigationPanelMappingConfig> navigationPanelMappingConfigs) {
        this.navigationPanelMappingConfigs = navigationPanelMappingConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NavigationPanelMappingsConfig)) return false;

        NavigationPanelMappingsConfig that = (NavigationPanelMappingsConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }
        if (navigationPanelMappingConfigs != null ? !navigationPanelMappingConfigs.equals(that.navigationPanelMappingConfigs) : that.
                navigationPanelMappingConfigs != null)  {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (navigationPanelMappingConfigs != null ? navigationPanelMappingConfigs.hashCode() : 0);
        return result;
    }
}
