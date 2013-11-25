package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;

import java.io.Serializable;


/**
 * Задаёт выражение на DOEL
 *
 * @author atsvetkov
 *
 */
public class DoelAware implements Serializable {

    @Element(name ="doel", required = true)
    private String doel;

    public String getDoel() {
        return doel;
    }

    public void setDoel(String doel) {
        this.doel = doel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DoelAware doelAware = (DoelAware) o;

        if (doel != null ? !doel.equals(doelAware.doel) : doelAware.doel != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return doel != null ? doel.hashCode() : 0;
    }
}
