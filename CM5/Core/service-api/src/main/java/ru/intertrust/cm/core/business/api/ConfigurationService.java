package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.config.ConfigurationExplorer;

/**
 * @author vmatsukevich
 *         Date: 9/12/13
 *         Time: 10:41 AM
 */
public interface ConfigurationService extends ConfigurationExplorer {

    public interface Remote extends ConfigurationService {
    }
}
