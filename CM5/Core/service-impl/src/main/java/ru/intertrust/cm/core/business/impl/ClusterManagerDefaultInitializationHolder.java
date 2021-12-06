package ru.intertrust.cm.core.business.impl;

import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.ClusterManagerInitializationHolder;

@Service
public class ClusterManagerDefaultInitializationHolder implements ClusterManagerInitializationHolder {

    private volatile boolean initialized;

    @Override
    public void setInitialized() {
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
}
