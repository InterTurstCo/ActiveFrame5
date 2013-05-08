package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:05 PM
 */
@Root
public class Configuration {
    @ElementList(inline = true)
    private List<BusinessObjectConfig> businessObjectConfigs;

    public List<BusinessObjectConfig> getBusinessObjectConfigs() {
        return businessObjectConfigs;
    }

    public void setBusinessObjectConfigs(List<BusinessObjectConfig> businessObjectConfigs) {
        this.businessObjectConfigs = businessObjectConfigs;
    }
}
