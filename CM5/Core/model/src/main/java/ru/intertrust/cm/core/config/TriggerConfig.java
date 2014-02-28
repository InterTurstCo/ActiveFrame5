package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * 
 * @author atsvetkov
 *
 */
public class TriggerConfig implements Dto {

    @Attribute(name = "domain-object-type", required = true)
    private String domainObjectType;

    @Attribute(name = "event", required = true)
    private String event;

    @Element(name = "config", required = false)
    private TriggerConfigConfig triggerConfig;
    
    @Element(name = "class-name", required = false)
    private TriggerClassNameConfig triggerClassNameConfig;

    @Element(name = "conditions-script", required = false)
    private TriggerConditionsScriptConfig triggerConditionsScriptConfig;
    
    public TriggerConfigConfig getTriggerConfig() {
        return triggerConfig;
    }

    public void setTriggerConfig(TriggerConfigConfig triggerConfig) {
        this.triggerConfig = triggerConfig;
    }
    
    public TriggerClassNameConfig getTriggerClassNameConfig() {
        return triggerClassNameConfig;
    }

    public void setTriggerClassNameConfig(TriggerClassNameConfig triggerClassNameConfig) {
        this.triggerClassNameConfig = triggerClassNameConfig;
    }

    public TriggerConditionsScriptConfig getTriggerConditionsScriptConfig() {
        return triggerConditionsScriptConfig;
    }

    public void setTriggerConditionsScriptConfig(TriggerConditionsScriptConfig triggerConditionsScriptConfig) {
        this.triggerConditionsScriptConfig = triggerConditionsScriptConfig;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domainObjectType == null) ? 0 : domainObjectType.hashCode());
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TriggerConfig other = (TriggerConfig) obj;
        if (domainObjectType == null) {
            if (other.domainObjectType != null) {
                return false;
            }
        } else if (!domainObjectType.equals(other.domainObjectType))
            return false;
        if (event == null) {
            if (other.event != null) {
                return false;
            }
        } else if (!event.equals(other.event)) {
            return false;
        }
        if (triggerClassNameConfig == null) {
            if (other.triggerClassNameConfig != null) {
                return false;
            }
        } else if (!triggerClassNameConfig.equals(other.triggerClassNameConfig)) {
            return false;
        }
        if (triggerConditionsScriptConfig == null) {
            if (other.triggerConditionsScriptConfig != null) {
                return false;
            }
        } else if (!triggerConditionsScriptConfig.equals(other.triggerConditionsScriptConfig)) {
            return false;
        }
        if (triggerConfig == null) {
            if (other.triggerConfig != null) {
                return false;
            }
        } else if (!triggerConfig.equals(other.triggerConfig)) {
            return false;
        }
        return true;
    }
    
    
}
