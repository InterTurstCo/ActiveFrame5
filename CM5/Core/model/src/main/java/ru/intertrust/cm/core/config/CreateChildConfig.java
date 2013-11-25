package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * Конфигурация разрешений на создание дочерних объектов заданного типа.
 * @author atsvetkov
 *
 */
public class CreateChildConfig extends BaseOperationPermitConfig {

    /**
     * Тип дочернего объекта, разрешения на создание которого выдаются.
     */
    @Attribute(required = true)
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CreateChildConfig that = (CreateChildConfig) o;

        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        if (getPermitConfigs() != null ? !getPermitConfigs().equals(that.getPermitConfigs()) : that.getPermitConfigs() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + super.hashCode();
        return result;
    }

}
