package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.ServerStatus;

public class ServerStatusImpl implements ServerStatus{
    private boolean init;
    
    @Override
    public boolean isInit() {
        return init;
    }

    @Override
    public void setInit(boolean value) {
        this.init = value;        
    }
}
