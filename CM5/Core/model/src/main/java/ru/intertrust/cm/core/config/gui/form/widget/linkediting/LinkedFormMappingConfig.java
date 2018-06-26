package ru.intertrust.cm.core.config.gui.form.widget.linkediting;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.10.2014
 *         Time: 17:46
 */
@Root(name = "linked-form-mapping")
public class LinkedFormMappingConfig implements Dto {
    @ElementList(name = "linked-form", inline = true)
    private List<LinkedFormConfig> linkedFormConfigs;

    public List<LinkedFormConfig> getLinkedFormConfigs() {
        if(linkedFormConfigs==null){
            linkedFormConfigs = new ArrayList<>();
        }
        return linkedFormConfigs;
    }

    public void setLinkedFormConfigs(List<LinkedFormConfig> linkedFormConfigs) {
        this.linkedFormConfigs = linkedFormConfigs;
    }

    public static List<FormMappingConfig> toFormMappingConfigList(List<LinkedFormConfig> linkedFormConfigs) {
        if (linkedFormConfigs == null) {
            return null;
        }
        ArrayList<FormMappingConfig> result = new ArrayList<>(linkedFormConfigs.size());
        for (LinkedFormConfig linkedFormConfig : linkedFormConfigs) {
            FormMappingConfig formMappingConfig = new FormMappingConfig();
            formMappingConfig.setDomainObjectType(linkedFormConfig.getDomainObjectType());
            formMappingConfig.setForm(linkedFormConfig.getName());
            result.add(formMappingConfig);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkedFormMappingConfig that = (LinkedFormMappingConfig) o;

        if (linkedFormConfigs != null ? !linkedFormConfigs.equals(that.linkedFormConfigs)
                : that.linkedFormConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return linkedFormConfigs != null ? linkedFormConfigs.hashCode() : 0;
    }
}
