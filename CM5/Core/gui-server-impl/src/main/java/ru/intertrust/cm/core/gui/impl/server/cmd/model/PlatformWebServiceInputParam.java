package ru.intertrust.cm.core.gui.impl.server.cmd.model;

import java.util.Map;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
public class PlatformWebServiceInputParam {
    private String beanName;
    private Map<String, String> data;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
