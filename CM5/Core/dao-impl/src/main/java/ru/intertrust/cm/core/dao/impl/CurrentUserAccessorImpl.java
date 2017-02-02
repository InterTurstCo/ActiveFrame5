package ru.intertrust.cm.core.dao.impl;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.dao.api.TicketService;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Реализация доступа к текущему пользователю, вошедшему в систему.
 * @author atsvetkov
 *
 */
public class CurrentUserAccessorImpl implements CurrentUserAccessor {

    final static Logger logger = LoggerFactory.getLogger(CurrentUserAccessorImpl.class);

    private EJBContext ejbContext;
    
    private ThreadLocal<String> ticketPerson = new ThreadLocal<String>();
    
    @Autowired
    private TicketService ticketService;

    private EJBContext getEjbContext() {
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

    /**
     * Возвращает логин текущего пользователя. Если пользователя нет в EJB контексте и в случае возникновения исключений
     * выозвращает null.
     * @return логин текущего пользователя
     */
    public String getCurrentUser() {
        if (ticketPerson.get() != null){
            return ticketPerson.get();
        }

        String result = null;
        try {
            // В случае если вызов идет изнутри представившись как system то подставляем пользователя admin,
            // возможно понадобится иметь иного системного пользователя
            // TODO разобратся почему не устанавливается роль

            // Workaround for JBoss7 bug in @RunAs
            EJBContext ejbContext = getEjbContext();

            if (logger.isDebugEnabled()) {
                Principal principal = ejbContext.getCallerPrincipal();
                logger.debug("Caller principal: " + (principal != null ? "present" : "absent"));
                if (principal != null) {
                    logger.debug("Principal name: " + principal.getName());
                }
                logger.debug("Roles: cm_user=" + (ejbContext.isCallerInRole("cm_user") ? "YES" : "no")
                        + "; system=" + (ejbContext.isCallerInRole("system") ? "YES" : "no"));
            }

            if (Boolean.TRUE.equals(ejbContext.getContextData().get(INITIAL_DATA_LOADING))) {
                return null;
            } else {            	
                String principalName = ejbContext.getCallerPrincipal().getName();
                if (!(principalName.equals("guest") || principalName.equals("anonymous") || principalName.equals("system"))){
                	result = principalName;
                }
                //if (principalName == null) {
                else if (ejbContext.isCallerInRole("system")) {
                    result = "admin";
                }
                else if (principalName.equals("system")){
                	result = "admin";
                }
                /*} else if (principalName.equals("anonymous")
                        || principalName.equals("guest")) {
                    // и JBoss 7, JBoss 6.x возвращают anonymous для @RunAs("system"); Apache TomEE возвращает guest.
                    // Даже если делать проверку на isCallerInRole("system") перед этим, то мы всё равно сюда попадём, если это не так.
                    // Вопрос, может ли кто-то со стороны ещё оказаться здесь как anonymous? Вряд ли.
                    result = "admin"; // TODO возможно стоит подумать над иным пользователем, например system
                }*/ else if (ejbContext.isCallerInRole("cm_user")) {
                    result = ejbContext.getCallerPrincipal().getName(); //principalName;
                }
            }
        } catch (Exception e) {
            result = null;
            //if (logger.isDebugEnabled()) {
                logger.debug("Error getting current user", e);
            //}
        }

        return result;
    }

    /**
     * Возвращает идентификатор текущего пользователя. Возвращает null, если невозможно получить пользователя из EJB
     * контекста.
     * @return идентификатор текущего пользователя.
     */
    public Id getCurrentUserId() {
        String login = getCurrentUser();
        if (login != null) {
            try {
                return getPersonServiceDao().findPersonByLogin(login).getId();
            } catch (Exception e) {
                logger.info("Error getting current user", e);
                return null;
            }
        } else {
            return null;
        }
    }

    private PersonServiceDao getPersonServiceDao() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(PersonServiceDao.class);
    }

    @Override
    public void setTicket(String ticket) {
        String person = ticketService.checkTicket(ticket);
        ticketPerson.set(person);        
    }

    @Override
    public void cleanTicket() {
        ticketPerson.remove();        
    }
}
