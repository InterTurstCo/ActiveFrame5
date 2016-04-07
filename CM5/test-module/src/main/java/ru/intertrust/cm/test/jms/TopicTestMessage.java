package ru.intertrust.cm.test.jms;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class TopicTestMessage implements Dto{
    private static final long serialVersionUID = 717365444621536272L;

    private int pause;

    public TopicTestMessage(int pause){
        this.pause = pause;
    }
    
    public int getPause() {
        return pause;
    }

    public void setPause(int pause) {
        this.pause = pause;
    }
}
