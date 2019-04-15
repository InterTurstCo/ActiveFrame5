package ru.intertrust.cm.core.gui.impl.server.action.access;

import java.util.HashSet;
import java.util.Set;

import ru.intertrust.cm.core.gui.api.server.authentication.SecurityConfig;

public class SecurityConfigImpl implements SecurityConfig{

    private Set<String> activeProviders = new HashSet<String>();
    
    @Override
    public Set<String> getActiveProviders() {
        return activeProviders;
    }

    public void setActiveProviders(Set<String> activeProviders) {
        this.activeProviders = activeProviders;
    }
}
