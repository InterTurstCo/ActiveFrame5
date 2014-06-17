package ru.intertrust.cm.core.business.impl;

/**
 * Business interface for InitialDataLoader
 * Created by vmatsukevich on 6/16/14.
 */
public interface InitialDataLoaderInterface {

    interface Remote extends InitialDataLoaderInterface {}

    void load() throws Exception;

}
