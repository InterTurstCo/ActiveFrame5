package ru.intertrust.cm.deployment.tool.xml;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Set;

/**
 * Created by Alexander Bogatyrenko on 09.08.16.
 * <p>
 * This class represents...
 */
@Root(name = "server", strict = false)
public class StandaloneXml {

    @ElementList(name = "system-properties")
    private Set<SystemProperty> systemProperty;

    public Set<SystemProperty> getSystemProperty() {
        return systemProperty;
    }
}
