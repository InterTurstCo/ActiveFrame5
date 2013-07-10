package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

import org.simpleframework.xml.Element;


/**
 * Задаёт выражение на DOEL
 * 
 * @author atsvetkov
 *
 */
public class DoelAware implements Serializable {

    @Element(name ="doel", required = false)    
    private String doel;

    public String getDoel() {
        return doel;
    }

    public void setDoel(String doel) {
        this.doel = doel;
    }
        
}
