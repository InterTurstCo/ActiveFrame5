package ru.intertrust.cm.core.business.api.notification;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;

/**
 * Интерфейс класса получателей адресатов для точек расширения отправляющих сообщения по возникновению события
 * @author larin
 * 
 */
public interface NotificationAddresseeReceiver {
    
    /**
     * Метод возвращает список адресатов
     * @param domainObject доменный объект по которому произошло событие
     * @return
     */
    List<NotificationAddressee> getNotificationAddressee(DomainObject domainObject);
}
