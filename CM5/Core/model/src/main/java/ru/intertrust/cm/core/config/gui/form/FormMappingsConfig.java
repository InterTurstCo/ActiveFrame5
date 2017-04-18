package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "form-mappings")
public class FormMappingsConfig implements Dto, TopLevelConfig {
    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @ElementList(inline = true)
    private List<FormMappingConfig> formMappingConfigList = new ArrayList<FormMappingConfig>();

    @ElementList(inline = true, required = false)
    private List<FormWidgetAccessConfig> formWidgetAccessConfig = new ArrayList<FormWidgetAccessConfig>();

    public String getName() {
       return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FormMappingConfig> getFormMappingConfigList() {
        return formMappingConfigList;
    }

    public void setFormMappingConfigList(List<FormMappingConfig> formMappingConfigList) {
        this.formMappingConfigList = formMappingConfigList;
    }

    public List<FormWidgetAccessConfig> getFormWidgetAccessConfig() {
        return formWidgetAccessConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormMappingsConfig that = (FormMappingsConfig) o;

        if (formMappingConfigList != null ? !formMappingConfigList.equals(that.formMappingConfigList) : that.
                formMappingConfigList != null) {
                    return false;
        }
        if (formWidgetAccessConfig != null ? !formWidgetAccessConfig.equals(that.formWidgetAccessConfig) : that.
                formWidgetAccessConfig != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 23 * result + (formMappingConfigList != null ? formMappingConfigList.hashCode() : 0);
        result = 23 * result + (formWidgetAccessConfig != null ? formWidgetAccessConfig.hashCode() : 0);
        return result;
    }
}
