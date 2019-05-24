package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Конфигурация индексов для доменных объектов.
 * @author atsvetkov
 */

public class IndicesConfig implements Dto {

    @ElementList(entry = "index", type = IndexConfig.class, inline = true, required = false)
    private List<IndexConfig> indices = new ArrayList<IndexConfig>();

    public List<IndexConfig> getIndices() {
        return indices;
    }

    public void setIndices(List<IndexConfig> indices) {
        if (indices != null) {
            this.indices = indices;
        } else {
            this.indices.clear();
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((indices == null) ? 0 : indices.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        IndicesConfig other = (IndicesConfig) o;
        if (indices == null) {
            if (other.indices != null) {
                return false;
            }
        } else if (!indices.equals(other.indices)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IndicesConfig [indices=" + indices + "]";
    }
    
    
}
