package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 13:23
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "markup")
public class WidgetTemplateConfig implements Dto {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }
}
