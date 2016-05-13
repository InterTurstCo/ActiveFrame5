package ru.intertrust.cm.loffice.impl;

import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;
import ru.intertrust.cm.core.pdf.PropertiesProvider;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 12.05.2016
 * Time: 15:23
 * To change this template use File | Settings | File and Code Templates.
 */
@ServerComponent(name = "default.converter.properties.provider")
public class DefaultConverterPropertiesProvider implements PropertiesProvider {

    private static final String PARAM_ACTIVE = "default.pdf.converter.active";
    private static final String PARAM_OFFICE = "default.pdf.converter.lofinstalldir";
    private static final String PARAM_WORKINGDIR = "default.pdf.converter.workingdir";

    private String libreOfficeBaseDir;
    private String workingDirectory;
    private Boolean converterActive;
    private Properties properties;


    @Value("${default.pdf.converter.lofinstalldir:not defined}")
    public void setLibreOfficeBaseDir(String libreOfficeBaseDir) {
        this.libreOfficeBaseDir = libreOfficeBaseDir;
    }

    public String getLibreOfficeBaseDir() {
        return libreOfficeBaseDir;
    }

    public Boolean getConverterActive() {
        return converterActive;
    }

    @Value("${default.pdf.converter.active:false}")
    public void setConverterActive(Boolean converterActive) {
        this.converterActive = converterActive;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Value("${default.pdf.converter.workingdir:not defined}")
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public Object getProperties() {
        properties = new Properties();
        if (converterActive) {
            properties.put(PARAM_ACTIVE,getConverterActive());
            properties.put(PARAM_OFFICE,getLibreOfficeBaseDir());
            properties.put(PARAM_WORKINGDIR,getWorkingDirectory());
        }
        return properties;
    }

}
