package ru.intertrust.cm.core.config.search;

import java.io.Serializable;

import org.simpleframework.xml.Element;

/**
 * Конфигурация фильтров для областей поиска
 * @author atsvetkov
 *
 */
public class DomainObjectFilterConfig implements Serializable {

    @Element(name = "class-name", required = false)
    private String javaClass;

    @Element(name = "search-query", required = false)
    private String searchQuery;

    @Element(name = "conditions-script", required = false)
    private String conditionsScript;

    public String getJavaClass() {
        return javaClass;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }

    public String getConditionsScript() {
        return conditionsScript;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditionsScript == null) ? 0 : conditionsScript.hashCode());
        result = prime * result + ((javaClass == null) ? 0 : javaClass.hashCode());
        result = prime * result + ((searchQuery == null) ? 0 : searchQuery.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DomainObjectFilterConfig other = (DomainObjectFilterConfig) obj;
        if (conditionsScript == null) {
            if (other.conditionsScript != null) {
                return false;
            }
        } else if (!conditionsScript.equals(other.conditionsScript)) {
            return false;
        }
        if (javaClass == null) {
            if (other.javaClass != null) {
                return false;
            }
        } else if (!javaClass.equals(other.javaClass)) {
            return false;
        }
        if (searchQuery == null) {
            if (other.searchQuery != null) {
                return false;
            }
        } else if (!searchQuery.equals(other.searchQuery)) {
            return false;
        }
        return true;
    }    
}
