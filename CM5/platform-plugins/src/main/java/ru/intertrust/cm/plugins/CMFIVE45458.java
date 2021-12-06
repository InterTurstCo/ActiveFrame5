package ru.intertrust.cm.plugins;

import javax.ejb.EJBContext;
import javax.ejb.TransactionManagement;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.dao.api.TicketService;


@Plugin(name = "CMFIVE45458",
        description = "Тест CMFIVE45458",
        transactional = false)
public class CMFIVE45458 implements PluginHandler {

    @Autowired
    CrudService crud;

    @Autowired
    TicketService ticketService;


    @Override
    public String execute(EJBContext ejbContext, String param) {
        String threadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("CMFIVE45458");
            if (param != null && !param.isEmpty() && param.equalsIgnoreCase("tm")) {
                thisWillFailUseTm(ejbContext);
            } else if (param != null && !param.isEmpty()) {
                thisWillFail();
            } else {
                thisWillFail(ejbContext);
            }
            return "OK";
        }finally {
            Thread.currentThread().setName(threadName);
        }
    }

    private void thisWillFailUseTm(EJBContext ejbContext) {
        TransactionManager tm = null;
        try {
            tm = InitialContext.doLookup("java:jboss/TransactionManager");
            tm.begin();
            test();
            tm.commit();
        } catch (Exception ex) {
            try {
                tm.rollback();
            } catch (Exception ignoreEx) {
            }
            throw new RuntimeException("Error", ex);
        }
    }

    @Transactional/*(propagation = Propagation.REQUIRES_NEW)*/
    protected void thisWillFail() {
        test();
    }

    protected void thisWillFail(EJBContext ejbContext) {
        try {
            ejbContext.getUserTransaction().begin();
            test();
            ejbContext.getUserTransaction().commit();
        } catch (Exception ex) {
            try {
                ejbContext.getUserTransaction().rollback();
            } catch (Exception ignoreEx) {
            }
            throw new RuntimeException("Error", ex);
        }
    }

    private void test(){
        // С этим ошибка
        DomainObject dobj = crud.createDomainObject("string_settings");
        dobj.setString("name", String.valueOf(System.currentTimeMillis()));
        crud.save(dobj);

        String ticket = ticketService.createTicket();
        ticketService.checkTicket(ticket);

        System.out.println("OK");
    }

}