package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 8:50 PM
 */
@Root(name = "domain-object")
public class DomainObjectConfig implements Serializable {

    private Long id;

    @Attribute(required = true)
    private String name;

    @Attribute(name = "extends", required = false)
    private String parentConfig;

    // we can't use a list here directly, as elements inside are different, that's why such a "trick"
    @Element(name = "fields")
    private DomainObjectFieldsConfig domainObjectFieldsConfig;

    @ElementList(entry="uniqueKey", type=UniqueKeyConfig.class, inline=true, required = false)
    private List<UniqueKeyConfig> uniqueKeyConfigs;

    public DomainObjectConfig() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentConfig() {
        return parentConfig;
    }

    public void setParentConfig(String parentConfig) {
        this.parentConfig = parentConfig;
    }

    public DomainObjectFieldsConfig getDomainObjectFieldsConfig() {
        return domainObjectFieldsConfig;
    }

    public void setDomainObjectFieldsConfig(DomainObjectFieldsConfig domainObjectFieldsConfig) {
        this.domainObjectFieldsConfig = domainObjectFieldsConfig;
    }

    public List<FieldConfig> getFieldConfigs() {
        if(domainObjectFieldsConfig == null) {
            domainObjectFieldsConfig = new DomainObjectFieldsConfig();
        }
        return domainObjectFieldsConfig.getFieldConfigs();
    }

    public List<UniqueKeyConfig> getUniqueKeyConfigs() {
        if(uniqueKeyConfigs == null) {
            uniqueKeyConfigs = new ArrayList<>();
        }
        return uniqueKeyConfigs;
    }

    public void setUniqueKeyConfigs(List<UniqueKeyConfig> uniqueKeyConfigs) {
        this.uniqueKeyConfigs = uniqueKeyConfigs;
    }
}
