package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.05.2015
 */
@Root(name="workflow-action")
public class WorkflowActionConfig extends AbstractActionConfig {

    @Attribute(name = "name", required = true)
    private String name;


    public String getName() {
        return name == null || name.isEmpty() ? null : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
