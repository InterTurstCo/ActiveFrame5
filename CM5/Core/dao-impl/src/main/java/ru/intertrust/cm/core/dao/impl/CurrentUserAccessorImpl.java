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
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Реализация доступа к текущему пользователю, вошедшему в систему.
 * @author atsvetkov
 *
 */
public class CurrentUserAccessorImpl implements CurrentUserAccessor {

    private Map<String, Id> personByLogin = new HashMap<String, Id>();

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
        return getEjbContext().getCallerPrincipal().getName();
    }
    
    public Id getCurrentUserId() {
        Id personId = null;
        String login = getCurrentUser();
        if (personByLogin.get(login) == null) {
            personId = getPersonManagementServiceDao().getPersonId(login);
            personByLogin.put(login, personId);
        } else {
            personId = personByLogin.get(login);

        }
        return personId;
    }
    
    private PersonManagementServiceDao getPersonManagementServiceDao() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(PersonManagementServiceDao.class);
    }

    
}
