package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:05 PM
 */
@Root
public class BusinessObjectsConfiguration implements Serializable {
    @ElementList(inline = true)
    private List<BusinessObjectConfig> businessObjectConfigs;

    public List<BusinessObjectConfig> getBusinessObjectConfigs() {
        if(businessObjectConfigs == null) {
            businessObjectConfigs = new ArrayList<BusinessObjectConfig>();
        }
        return businessObjectConfigs;
    }

    public void setBusinessObjectConfigs(List<BusinessObjectConfig> businessObjectConfigs) {
        this.businessObjectConfigs = businessObjectConfigs;
    }




}
