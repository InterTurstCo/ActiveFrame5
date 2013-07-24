package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Включает группу пользователей в состав роли.
 * @author atsvetkov
 *
 */
@Root(name = "group")
public class GroupConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    @Element(name = "bind-context", required = false)
    private BindContextConfig bindContext;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BindContextConfig getBindContext() {
        return bindContext;
    }

    public void setBindContext(BindContextConfig bindContext) {
        this.bindContext = bindContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupConfig that = (GroupConfig) o;

        if (bindContext != null ? !bindContext.equals(that.bindContext) : that.bindContext != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
