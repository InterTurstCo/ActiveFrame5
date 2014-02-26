package ru.intertrust.cm.core.business.impl;


import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;

@Stateless(name = "NotificationService")
@Local(NotificationService.class)
@Remote(NotificationService.Remote.class)
public class NotificationServiceImpl implements NotificationService{
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    @Override
    public void sendOnTransactionSuccess(String notificationType, Id sender, List<NotificationAddressee> addresseeList,
            NotificationPriority priority, NotificationContext context) {
        //TODO реализовать метод
        logger.info("TODO sendOnTransactionSuccess " + notificationType + " " + addresseeList);
    }

    @Override
    public Future<Boolean> sendNow(String notificationType, Id sender, List<NotificationAddressee> addresseeList,
            NotificationPriority priority, NotificationContext context) {
        //TODO реализовать метод
        logger.info("TODO sendNow " + notificationType + " " + addresseeList);
        return new AsyncResult<Boolean>(true);
    }

}
