package ru.intertrust.cm.core.gui.impl.server.services;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Local(TestBmtService.class)
@Remote(TestBmtService.Remote.class)
public class TestBmtServiceImpl implements TestBmtService{
    @EJB
    private TestCmtService cmtService;
    
    @Resource
    private EJBContext ejbContext;
    
    @Override
    public void execute(String name, ExceptionType exceptionType, boolean rolback) {
        try{
            ejbContext.getUserTransaction().begin();
            cmtService.executeWithException(name, exceptionType);
        }catch(Exception ex){
            if (rolback){
                try {
                    ejbContext.getUserTransaction().rollback();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    ejbContext.getUserTransaction().commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }                  
    }
    
}
