package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import ru.intertrust.cm.core.gui.impl.client.panel.RootNodeButton;
import ru.intertrust.cm.core.gui.model.counters.CounterKey;

/**
 * Created by andrey on 19.03.14.
 */
public class CounterRootNodeDecorator implements CounterDecorator {
    private RootNodeButton rootNodeButton;
    private String name;
    private String collectionName;
    private CounterKey counterKey;

    public CounterRootNodeDecorator(RootNodeButton rootButton) {
        this.rootNodeButton = rootButton;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public void decorate(Long counterValue) {
        Long counter = counterValue;
        if (counterValue == null) {
            counter = 0L;
        }
        rootNodeButton.writeHtml(counter,
                rootNodeButton.getName(),
                rootNodeButton.getImage(),
                rootNodeButton.getDisplayText());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setCounterKey(CounterKey counterKey) {
        this.counterKey = counterKey;
    }

    public CounterKey getCounterKey() {
        return counterKey;
    }
}
