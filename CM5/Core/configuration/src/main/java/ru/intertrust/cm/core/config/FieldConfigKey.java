package ru.intertrust.cm.core.config;

/**
* Created by vmatsukevich on 4/24/14.
*/
public class FieldConfigKey {

    private String domainObjectName;
    private String fieldConfigName;

    FieldConfigKey(String domainObjectName, String fieldConfigName) {
        this.domainObjectName = domainObjectName.toLowerCase();
        this.fieldConfigName = fieldConfigName.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldConfigKey that = (FieldConfigKey) o;

        if (domainObjectName != null ? !domainObjectName.equals(that.domainObjectName) :
                that.domainObjectName != null) {
            return false;
        }
        if (fieldConfigName != null ? !fieldConfigName.equals(that.fieldConfigName) :
                that.fieldConfigName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainObjectName != null ? domainObjectName.hashCode() : 0;
        result = 31 * result + (fieldConfigName != null ? fieldConfigName.hashCode() : 0);
        return result;
    }
}
