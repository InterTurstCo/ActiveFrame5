package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Case;

/**
* Created by vmatsukevich on 4/24/14.
*/
public class FieldConfigKey {

    private String domainObjectName;
    private String fieldConfigName;
    private boolean searchInHierarchy;

    FieldConfigKey(String domainObjectName, String fieldConfigName) {
        this.domainObjectName = Case.toLower(domainObjectName);
        this.fieldConfigName = Case.toLower(fieldConfigName);
    }

    public FieldConfigKey(String domainObjectName, String fieldConfigName, boolean searchInHierarchy) {
        this(domainObjectName, fieldConfigName);
        this.searchInHierarchy = searchInHierarchy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldConfigKey that = (FieldConfigKey) o;

        if (searchInHierarchy != that.searchInHierarchy) return false;
        if (!domainObjectName.equals(that.domainObjectName)) return false;
        if (!fieldConfigName.equals(that.fieldConfigName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainObjectName.hashCode();
        result = 31 * result + fieldConfigName.hashCode();
        result = 31 * result + (searchInHierarchy ? 1 : 0);
        return result;
    }
}
