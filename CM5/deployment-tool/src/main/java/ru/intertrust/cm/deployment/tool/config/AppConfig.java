package ru.intertrust.cm.deployment.tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.intertrust.cm.deployment.tool.property.RestoreVersionPropertyConverter;

import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.StringUtils.isEmpty;

@Configuration
@ComponentScan({"ru.intertrust.cm.deployment.tool"})
@PropertySource("file:upgrade.properties")
//if you run app from ide - uncomment this line and create upgrade.properties file in resources
//@PropertySource("classpath:upgrade.properties")
public class AppConfig {

    @Bean
    public Converter restoreVersionPropertyConverter() {
        return new RestoreVersionPropertyConverter();
    }

    @Bean
    public ConversionService conversionService() {
        Set<Converter> converterSet = new HashSet<>();
        converterSet.add(restoreVersionPropertyConverter());
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.setConverters(converterSet);
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    public Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    public Boolean isWindowsEnv() {
        String os = System.getProperty("os.name");
        if (!isEmpty(os)) {
            return os.contains("win");
        }
        return null;
    }
}