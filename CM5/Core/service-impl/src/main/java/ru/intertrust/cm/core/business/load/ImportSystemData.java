package ru.intertrust.cm.core.business.load;

/**
 * Business interface for ImportSystemDataImpl
 * Created by vmatsukevich on 6/17/14.
 */
public interface ImportSystemData {

    interface Remote extends ImportSystemData {}

    void load();
}
