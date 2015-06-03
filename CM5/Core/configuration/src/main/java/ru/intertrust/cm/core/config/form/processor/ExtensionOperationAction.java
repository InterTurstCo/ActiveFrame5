package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.IdentifiedConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.05.2015
 *         Time: 18:24
 */
public interface ExtensionOperationAction<T extends IdentifiedConfig> {
    void process(List<T> target, List<T> source,ExtensionOperationStatus operationStatus);

}
