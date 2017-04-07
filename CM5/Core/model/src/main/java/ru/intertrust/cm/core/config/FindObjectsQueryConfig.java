package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Text;

public class FindObjectsQueryConfig implements FindObjectsType{
    @Text
    private String data;

    public FindObjectsQueryConfig(){
    }

    public FindObjectsQueryConfig(String query){
        this.data = query;
    }
    
    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FindObjectsQueryConfig that = (FindObjectsQueryConfig) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}
