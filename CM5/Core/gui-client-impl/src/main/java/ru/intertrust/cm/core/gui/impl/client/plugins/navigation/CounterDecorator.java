package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import ru.intertrust.cm.core.gui.model.counters.CounterKey;

/**
 * Created by andrey on 19.03.14.
 */
public interface CounterDecorator {
    void decorate(Long counterValue);
    CounterKey getCounterKey();
}
