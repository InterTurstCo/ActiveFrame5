package ru.intertrust.cm.test.jms;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ru.intertrust.cm.core.business.impl.JmsUtils;

@Stateless(name = "TestService")
@Remote(TestService.Remote.class)
public class TestServiceImpl implements TestService {

    @Override
    public void sendMessageToTopic(int pause) {
        try {
            JmsUtils.sendTopicMessage(new TopicTestMessage(pause), "topic/TestTopic");
        } catch (Exception ex) {
            throw new RuntimeException("Error send Message To Topic", ex);
        }
    }

    @Override
    public void cleanMessageCounter() {
        try {
            JmsUtils.sendTopicMessage(new TopicTestMessage(-1), "topic/TestTopic");
        } catch (Exception ex) {
            throw new RuntimeException("Error send clean test message counter", ex);
        }
    }

}
