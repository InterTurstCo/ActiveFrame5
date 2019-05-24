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
public class DomainObjectFieldsConfig implements Serializable {
    @ElementListUnion({
            @ElementList(entry="boolean", type=BooleanFieldConfig.class, inline=true, required = false),
            @ElementList(entry="long", type=LongFieldConfig.class, inline=true, required = false),
            @ElementList(entry="decimal", type=DecimalFieldConfig.class, inline=true, required = false),
            @ElementList(entry="date-time", type=DateTimeFieldConfig.class, inline=true, required = false),
            @ElementList(entry="dateTimeWithTimeZone", type=DateTimeWithTimeZoneFieldConfig.class, inline=true, required = false),
            @ElementList(entry="timelessDate", type=TimelessDateFieldConfig.class, inline=true, required = false),
            @ElementList(entry="string", type=StringFieldConfig.class, inline=true, required = false),
            @ElementList(entry="text", type=TextFieldConfig.class, inline=true, required = false),
            @ElementList(entry="reference", type=ReferenceFieldConfig.class, inline=true, required = false)
    })
    private List<FieldConfig> fieldConfigs = new ArrayList<>();

    @ElementListUnion({
        @ElementList(entry="include-group", type=IncludeFieldGroupConfig.class, inline=true, required = false)            
    })
    protected List<IncludeFieldGroupConfig> includeGroups = new ArrayList<>();
    
    public List<IncludeFieldGroupConfig> getIncludeGroups() {
        return includeGroups;
    }

    public void setIncludeGroups(List<IncludeFieldGroupConfig> includeGroups) {
        this.includeGroups = includeGroups;
    }

    public List<FieldConfig> getFieldConfigs() {
        return fieldConfigs;
    }

    public void setFieldConfigs(List<FieldConfig> fieldConfigs) {
        if(fieldConfigs != null) {
            this.fieldConfigs = fieldConfigs;
        } else {
            this.fieldConfigs.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainObjectFieldsConfig that = (DomainObjectFieldsConfig) o;

        if (fieldConfigs != null ? !fieldConfigs.equals(that.fieldConfigs) : that.fieldConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return fieldConfigs != null ? fieldConfigs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DomainObjectFieldsConfig [fieldConfigs=" + fieldConfigs + ", includeGroups=" + includeGroups + "]";
    }
    
    
}
