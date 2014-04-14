package ru.intertrust.cm.core.config.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource({ "classpath:/server-default.properties","file:${server.properties.location}/server.properties" })
public class ServerProperties {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setNullValue("");
        //configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }
}
