package ru.intertrust.cm.core.gui.impl.server.services;


public interface TestBmtService {
    public interface Remote extends TestBmtService {
    }
    
    void execute(String name, ExceptionType exceptionType, boolean rolback);
}
