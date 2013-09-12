package ru.intertrust.cm.performance.dataset;


import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;


/**
 * 
 * @author erentsov
 *
 */
@Local(DatasetGenerationService.class)
@Remote(DatasetGenerationService.Remote.class)
@Stateless

public class DatasetGenerationServiceImpl implements DatasetGenerationService, DatasetGenerationService.Remote {
    public String execute(byte[] command){
        return null;
    }

}

