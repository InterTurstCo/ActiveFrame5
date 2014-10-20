package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@Root(name = "single-choice")
public class SingleChoiceConfig implements Dto {
    @Attribute(name = "value", required = false)
    private Boolean singleChoice;

    @Element(name = "parent-object-field", required = false)
    private ParentObjectFieldConfig parentObjectFieldConfig;

    public Boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(Boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    public ParentObjectFieldConfig getParentObjectFieldConfig() {
        return parentObjectFieldConfig;
    }

    public void setParentObjectFieldConfig(ParentObjectFieldConfig parentObjectFieldConfig) {
        this.parentObjectFieldConfig = parentObjectFieldConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SingleChoiceConfig that = (SingleChoiceConfig) o;

        if (singleChoice != null ? !singleChoice.equals(that.singleChoice): that.singleChoice != null) {
            return false;
        }
        if (parentObjectFieldConfig != null ? !parentObjectFieldConfig.equals(that.parentObjectFieldConfig)
                : that.parentObjectFieldConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = singleChoice != null ? singleChoice.hashCode() : 0;
        result = 31 * result + (parentObjectFieldConfig != null ? parentObjectFieldConfig.hashCode() : 0);
        return result;
    }
}

