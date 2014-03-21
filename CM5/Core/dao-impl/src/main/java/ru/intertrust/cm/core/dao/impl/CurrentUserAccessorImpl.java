package ru.intertrust.cm.core.dao.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Реализация доступа к текущему пользователю, вошедшему в систему.
 * @author atsvetkov
 *
 */
public class CurrentUserAccessorImpl implements CurrentUserAccessor {

    private EJBContext ejbContext;

    public EJBContext getEjbContext() {
        if (ejbContext == null) {
            try {
                InitialContext ic = new InitialContext();
                ejbContext = (SessionContext) ic.lookup("java:comp/EJBContext");
            } catch (NamingException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return ejbContext;
    }    

    public String getCurrentUser() {
        String result = null;
        //В случае если вызов идет изнутри представившись как system то подставляем пользователя admin, 
        //возможно понадобится иметь иного системного пользователя
        //TODO разобратся почему не устанавливается роль
        if (getEjbContext().isCallerInRole("system") || getEjbContext().getCallerPrincipal().getName().equals("anonymous")){
            //TODO возможно стоит подумать над иным пользователем, например system
            result = "admin";
        }else{
            result = getEjbContext().getCallerPrincipal().getName();
        }
        return result;
    }
    
    public Id getCurrentUserId() {
        String login = getCurrentUser();
        return getPersonServiceDao().findPersonByLogin(login).getId();
    }
    
    private PersonServiceDao getPersonServiceDao() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(PersonServiceDao.class);
    }

    
}
