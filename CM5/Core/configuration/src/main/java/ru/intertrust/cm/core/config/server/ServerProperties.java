package ru.intertrust.cm.core.config.server;

import java.io.File;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Configuration
public class ServerProperties {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setNullValue("");
        //configurer.setIgnoreUnresolvablePlaceholders(true);

        // !!!!! Динамическая загрузка проперти файлов переехали в класс ServerPropertiesInitializer, 
        //при текущей реализации не устанавливались свойства в объекте Environment
        
        
        
        String serverPropertiesLocation = System.getProperty("server.properties.location");

        String appName = "";
        try {
            appName = (String) new InitialContext().lookup("java:app/AppName");
        } catch (NamingException e) {
        }

        FileSystemResource serverPropertiesResource;

        File applicationSpecificServerProp = new File(serverPropertiesLocation, appName + "-server.properties");
        if (applicationSpecificServerProp.exists()){
            serverPropertiesResource = new FileSystemResource(applicationSpecificServerProp);
        } else {
            serverPropertiesResource = new FileSystemResource(serverPropertiesLocation + "/server.properties");
        }


        Resource[] resources = new Resource[ ]
                {
                        new ClassPathResource( "server-default.properties" ),
                        serverPropertiesResource
                };

        configurer.setLocations(resources);
        return configurer;
    }
    
    
}
