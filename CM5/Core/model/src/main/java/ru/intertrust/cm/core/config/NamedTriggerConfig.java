package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

@Root(name = "named-trigger")
public class NamedTriggerConfig  implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Element(name = "trigger", required = true)
    private TriggerConfig trigger = new TriggerConfig();    

    public TriggerConfig getTrigger() {
        return trigger;
    }

    public void setTrigger(TriggerConfig trigger) {
        this.trigger = trigger;
    }

    @Override
    public String getName() {
        return name;
    }

}
