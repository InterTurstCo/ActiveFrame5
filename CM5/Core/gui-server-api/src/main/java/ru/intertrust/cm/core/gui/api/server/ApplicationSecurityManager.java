package ru.intertrust.cm.core.gui.api.server;

import java.util.List;

import ru.intertrust.cm.core.gui.api.server.authentication.AuthenticationProvider;
import ru.intertrust.cm.core.gui.api.server.authentication.SecurityConfig;

public interface ApplicationSecurityManager {
    
    public static final String BASIC_AUTHENTICATION_TYPE = "basic";
    public static final String FORM_AUTHENTICATION_TYPE = "form";    
    public static final String LOGIN_FORM_DATA = "login.form.data";
    
    void addAuthenticationProvider(String name, AuthenticationProvider authenticationProvider);

    List<AuthenticationProvider> getAuthenticationProviders();
    
    void setSecurityConfig(SecurityConfig securityConfig);

    SecurityConfig getSecurityConfig();
}
