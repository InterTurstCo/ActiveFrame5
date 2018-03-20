package ru.intertrust.cm.core.dao.api;

public interface ServerStatus {
    boolean isInit();
    
    void setInit(boolean value);
}
