package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Text;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * 
 * @author atsvetkov
 *
 */
public class TriggerClassNameConfig  implements Dto {

    @Text
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
