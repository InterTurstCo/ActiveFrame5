package ru.intertrust.cm.core.business.impl;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;

@Stateless(name = "PersonService")
@Local(PersonService.class)
@Remote(PersonService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PersonServiceImpl implements PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

    @Autowired
    private PersonServiceDao personServiceDao;
    
    @Autowired
    private CurrentUserAccessor currentUserAccessor;    

    @Override
    public DomainObject findPersonByLogin(String login) {
        try {
            return personServiceDao.findPersonByLogin(login);
        } catch (SystemException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findPersonByLogin", ex);
            throw new UnexpectedException("PersonService", "findPersonByLogin",
                    "login:" + login, ex);
        }
    }

    @Override
    public DomainObject getCurrentPerson() {
        return findPersonByLogin(getCurrentPersonUid());
    }

    public String getCurrentPersonUid() {
        return currentUserAccessor.getCurrentUser();
    }
}
