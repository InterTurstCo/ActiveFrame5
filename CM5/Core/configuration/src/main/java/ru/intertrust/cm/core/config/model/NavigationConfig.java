package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import java.util.List;

/**
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:01
 */
@Root(name = "configuration", strict = false)
public class NavigationConfig implements TopLevelConfig {

    @ElementList(name ="navigation", required = false)
    private List<LinkConfig>  linkConfigList;

    @Override
    public String getName() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<LinkConfig> getLinkConfigList() {
        return linkConfigList;
    }

    public void setLinkConfig(List<LinkConfig> linkConfigList ) {
        this.linkConfigList = linkConfigList;
    }
}
