package ru.intertrust.cm.core.business.impl;

/**
 * Business interface for InitialDataLoaderImpl
 * Created by vmatsukevich on 6/16/14.
 */
public interface InitialDataLoader {

    interface Remote extends InitialDataLoader {}

    void load() throws Exception;

}
