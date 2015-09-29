package ru.intertrust.cm.core.gui.impl.server.services;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.model.FatalException;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(TestCmtService.class)
@Remote(TestCmtService.Remote.class)
public class TestCmtServiceImpl implements TestCmtService{

    @EJB
    private TestCmtService cmtService;
    
    @EJB
    private CrudService crudService;
    
    @Resource
    private EJBContext ejbContext;
    
    @Override
    public void executeWithException(String name, ExceptionType exceptionType) {
        DomainObject testDo = crudService.createDomainObject("test_type_15");
        testDo.setString("name", name);
        crudService.save(testDo);
        
        switch (exceptionType) {
            case FatalException:
                throw new FatalException("Test fatal exception");
            case SystemException:
                throw new GuiException("Test system exception");
            case RuntumeException:
                throw new RuntimeException("Test runtime exception");
            default:
                throw new UnsupportedOperationException();
        }        
    }

    @Override
    public void executeAndTry(String name, ExceptionType exceptionType, boolean rolback) {
        try{
            cmtService.executeWithException(name, exceptionType);
        }catch(Exception ex){
            if (rolback){
                throw new FatalException("Test fatal exception");
            }
        }        
    }

}
