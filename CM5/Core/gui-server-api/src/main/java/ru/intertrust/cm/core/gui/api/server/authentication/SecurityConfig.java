package ru.intertrust.cm.core.gui.api.server.authentication;

import java.util.Set;

public interface SecurityConfig {
    Set<String> getActiveProviders();
}
