package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Text;

public class FindObjectsClassConfig implements FindObjectsType {
    
    @Text
    private String data;

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }
}
