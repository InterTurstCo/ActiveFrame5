package ru.intertrust.cm.core.gui.impl.server.access;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Сервис получения информации о текущем пользователе
 * @author larin
 *
 */
@RestController
public class CurrentUserInfo {
    private static final Logger logger = LoggerFactory.getLogger(CurrentUserInfo.class);

    @Autowired
    private PersonService personService;

    public CurrentUserInfo() {
        logger.info("Init Current User Info Service");
    }

    @RequestMapping(value = "/security/curentuser", method = RequestMethod.GET)
    public Map<String, String> getPersonInfo() {
        try {
            logger.info("Current User Info start");
            Map<String, String> result = new HashMap<String, String>();
            result.put("uid", personService.getCurrentPersonUid());
            result.put("id", personService.getCurrentPerson().getId().toStringRepresentation());
            return result;
        } catch (Exception ex) {
            throw new FatalException("Error execute  Current User Info command", ex);
        }
    }

}
