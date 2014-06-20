package ru.intertrust.cm.core.business.load;

/**
 * Business interface for ImportReportsDataImpl
 * Created by vmatsukevich on 6/17/14.
 */
public interface ImportReportsData {

    interface Remote extends ImportReportsData {}

    void load();
}
