package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.Random;

/**
 * @author Denis Mitavskiy
 *         Date: 28.04.2017
 *         Time: 17:41
 */
public class ConfigurationUpdateMessage implements Dto {
    public static final long NODE_ID = new Random().nextLong();

    private long senderId;
    private TopLevelConfig topLevelConfig;

    public ConfigurationUpdateMessage() {
        senderId = NODE_ID;
    }

    public ConfigurationUpdateMessage(long senderId, TopLevelConfig topLevelConfig) {
        this.senderId = senderId;
        this.topLevelConfig = topLevelConfig;
    }

    public boolean fromThisNode() {
        return this.senderId == NODE_ID;
    }

    public TopLevelConfig getTopLevelConfig() {
        return topLevelConfig;
    }
}
