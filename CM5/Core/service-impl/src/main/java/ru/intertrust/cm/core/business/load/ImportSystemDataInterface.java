package ru.intertrust.cm.core.business.load;

/**
 * Business interface for ImportSystemData
 * Created by vmatsukevich on 6/17/14.
 */
public interface ImportSystemDataInterface {

    interface Remote extends ImportSystemDataInterface {}

    void load();
}
