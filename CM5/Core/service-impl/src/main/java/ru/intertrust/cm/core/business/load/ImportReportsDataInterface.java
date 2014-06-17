package ru.intertrust.cm.core.business.load;

/**
 * Business interface for ImportReportsData
 * Created by vmatsukevich on 6/17/14.
 */
public interface ImportReportsDataInterface {

    interface Remote extends ImportReportsDataInterface {}

    void load();
}
