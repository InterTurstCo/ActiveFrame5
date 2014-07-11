package ru.intertrust.cm.core.config.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;

import ru.intertrust.cm.core.model.FatalException;

public class ServerPropertiesInitializer  {
    @Autowired
    ApplicationContext context;
    
    @PostConstruct
    public void initialize() {
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) context;
        try {
            List<PropertySource> properties = getPropertiesResources();
            for (int i = 0; i < properties.size(); i++) {
                applicationContext.getEnvironment().getPropertySources().addFirst(properties.get(i));
            }
        } catch (Exception ex) {
            throw new FatalException("Error init server properties", ex);
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
            serverPropertiesResource = new FileSystemResource(serverPropertiesLocation + "/server.properties");
        }

        List<PropertySource> resources = new ArrayList<PropertySource>();
        resources.add(new ResourcePropertySource(new ClassPathResource("server-default.properties")));
        resources.add(new ResourcePropertySource(serverPropertiesResource));

        return resources;

    }
}
