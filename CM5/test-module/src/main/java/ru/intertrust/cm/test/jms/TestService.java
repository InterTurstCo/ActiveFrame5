package ru.intertrust.cm.test.jms;

public interface TestService {
    
    public interface Remote extends TestService {
    }
    
    void sendMessageToTopic(int pause);
    
    void cleanMessageCounter();
}
