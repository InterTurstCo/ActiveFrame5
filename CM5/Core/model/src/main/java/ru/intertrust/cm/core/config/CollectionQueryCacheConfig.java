package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionQueryCacheConfig implements Dto {

    @Attribute(name = "max-size", required = true)
    private Integer maxSize;
    
    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maxSize == null) ? 0 : maxSize.hashCode());
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
        CollectionQueryCacheConfig other = (CollectionQueryCacheConfig) obj;
        if (maxSize == null) {
            if (other.maxSize != null) {
                return false;
            }
        } else if (!maxSize.equals(other.maxSize)) {
            return false;
        }
        return true;
    }
}
