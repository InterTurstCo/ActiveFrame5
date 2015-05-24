package ru.intertrust.cm.core.config.gui.form.extension;

import ru.intertrust.cm.core.config.gui.IdentifiedConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ExtensionPlace;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 17:44
 */
public interface IdentifiedFormExtensionOperation<T extends IdentifiedConfig> {
    String getId();
    List<T> getSource();
    ExtensionPlace getExtensionPlace();
}
