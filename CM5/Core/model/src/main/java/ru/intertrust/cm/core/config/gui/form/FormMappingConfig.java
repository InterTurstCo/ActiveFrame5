package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.GroupsConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "form-mapping")
public class FormMappingConfig implements Dto {
    @Attribute(name = "form", required = false)
    private String form;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @ElementList(name = "field-path", required = false, inline = true)
    List<FieldPathConfig> fieldPathConfigs = new ArrayList<>();

    @Element(name = "users", required = false)
    private UsersConfig usersConfig;

    @Element(name = "groups", required = false)
    private GroupsConfig groupsConfig;

    @Attribute(name = "modal-width", required = false)
    private String modalWidth;

    @Attribute(name = "modal-height", required = false)
    private String modalHeight;

    public UsersConfig getUsersConfig() {
        return usersConfig;
    }

    public void setUsersConfig(UsersConfig usersConfig) {
        this.usersConfig = usersConfig;
    }

    public GroupsConfig getGroupsConfig() {
        return groupsConfig;
    }

    public void setGroupsConfig(GroupsConfig groupsConfig) {
        this.groupsConfig = groupsConfig;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public String getModalWidth() {
        return modalWidth;
    }

    public void setModalWidth(String modalWidth) {
        this.modalWidth = modalWidth;
    }

    public String getModalHeight() {
        return modalHeight;
    }

    public void setModalHeight(String modalHeight) {
        this.modalHeight = modalHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormMappingConfig that = (FormMappingConfig) o;
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null)
            return false;
        if (fieldPathConfigs != null ? !fieldPathConfigs.equals(that.fieldPathConfigs) : that.fieldPathConfigs != null)
            return false;
        if (form != null ? !form.equals(that.form) : that.form != null) return false;
        if (groupsConfig != null ? !groupsConfig.equals(that.groupsConfig) : that.groupsConfig != null) return false;
        if (usersConfig != null ? !usersConfig.equals(that.usersConfig) : that.usersConfig != null) return false;
        if (modalWidth != null ? !modalWidth.equals(that.modalWidth) : that.modalWidth != null) return false;
        if (modalHeight != null ? !modalHeight.equals(that.modalHeight) : that.modalHeight != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = form != null ? form.hashCode() : 0;
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        return result;
    }

    public List<FieldPathConfig> getFieldPathConfigs() {
        return fieldPathConfigs;
    }

    public void setFieldPathConfigs(List<FieldPathConfig> fieldPathConfigs) {
        this.fieldPathConfigs = fieldPathConfigs;
    }
}
