package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * Конфигурация выражений для индексов. Выражение может включать функции и операции над полями ДО.
 * @author atsvetkov
 *
 */
public class IndexExpressionConfig extends BaseIndexExpressionConfig {

    @Attribute(name = "value")
    private String value;

    public IndexExpressionConfig() {
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        IndexExpressionConfig other = (IndexExpressionConfig) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IndexExpressionConfig [value=" + value + "]";
    }
}
