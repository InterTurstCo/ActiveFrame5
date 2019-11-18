package ru.intertrust.cm.core.gui.impl.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.gui.api.server.ApplicationSecurityManager;
import ru.intertrust.cm.core.gui.api.server.authentication.AuthenticationProvider;
import ru.intertrust.cm.core.gui.api.server.authentication.SecurityConfig;
import ru.intertrust.cm.core.gui.impl.server.action.access.SecurityConfigImpl;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Singleton(name = "ApplicationSecurityManager")
@Interceptors(SpringBeanAutowiringInterceptor.class)
@Startup
public class ApplicationSecurityManagerImpl implements ApplicationSecurityManager {

    private Map<String, AuthenticationProvider> authenticationProviders = new HashMap<String, AuthenticationProvider>();
    private SecurityConfig securityConfig;

    @Value("${af5.authentication.type:}")
    private String authenticationTypes;
    
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    private void init() {
        // Поиск всех AuthenticationProvider в спринг контексте, и автоматическое их добавление;
        String[] authenticationProviderNames = context.getBeanNamesForType(AuthenticationProvider.class);
        for (String authenticationProviderName : authenticationProviderNames) {
            AuthenticationProvider authenticationProvider = (AuthenticationProvider)context.getBean(authenticationProviderName);
            authenticationProviders.put(authenticationProviderName, authenticationProvider);
        }
        
        // Настройка конфигурации
        securityConfig = new SecurityConfigImpl();

        if (authenticationTypes != null && !authenticationTypes.isEmpty() ) {
            // если есть настройка то доступны только указанные там способы аутентификации
            String[] authenticationTypeArr = authenticationTypes.split("[;, ]");
            for (String authenticationType : authenticationTypeArr) {
                securityConfig.getActiveProviders().add(authenticationType.trim());
            }
        } else {
            // По умолчанию доступны 2 способа аутентификации
            securityConfig.getActiveProviders().add(FORM_AUTHENTICATION_TYPE);
            securityConfig.getActiveProviders().add(BASIC_AUTHENTICATION_TYPE);
        }
    }

    @Override
    public void addAuthenticationProvider(String name, AuthenticationProvider authenticationProvider) {
        authenticationProviders.put(name, authenticationProvider);
    }

    @Override
    @Lock(LockType.READ)
    public List<AuthenticationProvider> getAuthenticationProviders() {
        List<AuthenticationProvider> result = new ArrayList<AuthenticationProvider>();
        for (String providerName : securityConfig.getActiveProviders()) {
            AuthenticationProvider activeProvider = authenticationProviders.get(providerName);
            if (activeProvider != null) {
                result.add(activeProvider);
            }
        }
        return result;
    }

    @Override
    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;

    }

    @Override
    @Lock(LockType.READ)
    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

}
