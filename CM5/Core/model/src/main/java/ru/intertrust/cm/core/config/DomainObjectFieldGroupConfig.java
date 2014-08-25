package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * 
 * @author atsvetkov
 *
 */
@Root(name = "field-group")
public class DomainObjectFieldGroupConfig extends DomainObjectFieldsConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Override
    public String getName() {
        return name;
    }

}
