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

    @ElementList(inline = true)
    private List<NavigationPanelMappingConfig> navigationPanelMappingConfigs = new ArrayList<NavigationPanelMappingConfig>();

    public String getName() {
        return name;
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
        if (o == null || getClass() != o.getClass()) return false;

        NavigationPanelMappingsConfig that = (NavigationPanelMappingsConfig) o;

        if (!navigationPanelMappingConfigs.equals(that.navigationPanelMappingConfigs)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + navigationPanelMappingConfigs.hashCode();
        return result;
    }
}
