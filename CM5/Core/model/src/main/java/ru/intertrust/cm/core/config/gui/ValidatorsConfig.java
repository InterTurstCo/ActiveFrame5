package ru.intertrust.cm.core.config.gui;

import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 10.03.14
 *         Time: 13:19
 */
public class ValidatorsConfig implements Dto {

    @ElementList(required=true, inline=true)
    ArrayList<ValidatorConfig> validators = new ArrayList<ValidatorConfig>();

    public List<ValidatorConfig> getValidators() {
        return validators;
    }

    public void setValidators(ArrayList<ValidatorConfig> validators) {
        this.validators = validators;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidatorsConfig that = (ValidatorsConfig) o;
        if (validators != null ? !validators.equals(that.validators) : that.validators != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return validators != null ? validators.hashCode() : 0;
    }

    @Override
    public String toString() {
        return validators.toString();
    }
}
