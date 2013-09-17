package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.model.gui.navigation.LinkConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */

public class NavigationConfigLightweight implements Serializable {

    private List<LinkConfig> linkConfigList = new ArrayList<>();

    private String name;

    private boolean isDefault;

    public String getName() {
        return name;
    }

    public NavigationConfigLightweight() {
    }

    public List<LinkConfig> getLinkConfigList() {
        return linkConfigList;
    }

    public void setLinkConfigList(List<LinkConfig> linkConfigList) {
        this.linkConfigList = linkConfigList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

}

