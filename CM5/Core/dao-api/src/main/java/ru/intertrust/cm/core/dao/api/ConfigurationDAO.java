package ru.intertrust.cm.core.dao.api;

/**
 * @author vmatsukevich
 *         Date: 6/17/13
 *         Time: 3:42 PM
 */
public interface ConfigurationDAO {

    void save(String configuration);

    String readLastLoadedConfiguration();
}
