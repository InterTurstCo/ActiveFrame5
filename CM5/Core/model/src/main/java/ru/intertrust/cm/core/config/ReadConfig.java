package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import java.util.List;

/**
 * Конфигурация разрешений на чтение объекта.
 * @author atsvetkov
 *
 */
public class ReadConfig extends BaseOperationPermitConfig {

    public ReadConfig(){
    }

    public ReadConfig(List<BasePermit> permitConfigs, Boolean permitEverybody){
        super(permitConfigs);
        this.permitEverybody = permitEverybody;
    }

    /**
     * Разрешает операцию чтения для всех персон.
     */
    @Attribute(name= "permit-everybody", required = false)
    private Boolean permitEverybody;

    @Deprecated
    public Boolean isPermitEverybody() {
        return permitEverybody;
    }

    public void setPermitEverybody(Boolean permitEverybody) {
        this.permitEverybody = permitEverybody;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReadConfig that = (ReadConfig) o;

        if (permitEverybody != that.permitEverybody ) {
            return false;
        }

        if (getPermitConfigs() != null ? !getPermitConfigs().equals(that.getPermitConfigs()) : that.getPermitConfigs() != null) {
            return false;
        }

        return true;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
