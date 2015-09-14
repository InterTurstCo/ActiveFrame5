package ru.intertrust.cm.core.gui.impl.server.services;


public interface TestCmtService {
    public interface Remote extends TestCmtService {
    }
    
    void executeWithException(String name, ExceptionType exceptionType);
    void executeAndTry(String name, ExceptionType exceptionType, boolean rolback);
}
