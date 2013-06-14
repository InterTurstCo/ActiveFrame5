package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:54 PM
 */
public class BusinessObjectFieldsConfig implements Serializable {
    @ElementListUnion({
            @ElementList(entry="long", type=LongFieldConfig.class, inline=true),
            @ElementList(entry="decimal", type=DecimalFieldConfig.class, inline=true),
            @ElementList(entry="dateTime", type=DateTimeFieldConfig.class, inline=true),
            @ElementList(entry="string", type=StringFieldConfig.class, inline=true),
            @ElementList(entry="password", type=StringFieldConfig.class, inline=true),
            @ElementList(entry="reference", type=ReferenceFieldConfig.class, inline=true)
    })
    private List<FieldConfig> fieldConfigs;

    public List<FieldConfig> getFieldConfigs() {
        if(fieldConfigs == null) {
            fieldConfigs = new ArrayList<FieldConfig>();
        }
        return fieldConfigs;
    }

    public void setFieldConfigs(List<FieldConfig> fieldConfigs) {
        this.fieldConfigs = fieldConfigs;
    }
}
