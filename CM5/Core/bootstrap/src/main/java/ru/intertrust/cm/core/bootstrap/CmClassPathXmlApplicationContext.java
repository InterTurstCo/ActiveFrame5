package ru.intertrust.cm.core.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;

public class CmClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {

    public CmClassPathXmlApplicationContext(String... configLocations) throws BeansException {
        super(configLocations);
    }
    
    @Override
    protected ConfigurableEnvironment createEnvironment() {
        try {
            ConfigurableEnvironment result = super.createEnvironment();

            List<PropertySource> properties = getPropertiesResources();
            for (int i = 0; i < properties.size(); i++) {
                result.getPropertySources().addFirst(properties.get(i));
            }

            return result;
        } catch (Exception ex) {
            throw new RuntimeException("Error init server properties", ex);
        }
    }

    private List<PropertySource> getPropertiesResources() throws IOException {
        String serverPropertiesLocation = System.getProperty("server.properties.location");

        String appName = "";
        try {
            appName = (String) new InitialContext().lookup("java:app/AppName");
        } catch (NamingException e) {
        }

        FileSystemResource serverPropertiesResource;

        File applicationSpecificServerProp = new File(serverPropertiesLocation, appName + "-server.properties");
        if (applicationSpecificServerProp.exists()) {
            serverPropertiesResource = new FileSystemResource(applicationSpecificServerProp);
        } else {
            serverPropertiesResource = new FileSystemResource(new File(serverPropertiesLocation, "server.properties"));
        }

        List<PropertySource> resources = new ArrayList<PropertySource>();
        resources.add(new ResourcePropertySource(new ClassPathResource("server-default.properties")));
        resources.add(new ResourcePropertySource(serverPropertiesResource));

        return resources;
    }
}
