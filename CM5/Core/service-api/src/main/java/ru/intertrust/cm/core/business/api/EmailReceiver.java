package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.email.EmailReceiverConfig;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Сервис просмотра ящиков электронной почты
 * @author larin
 *
 */
public interface EmailReceiver {
    /**
     * Получение всех писем из ящика, настройки которого приходят в параметре
     * config
     * @param config конфигурация подключенияк ящикам
     * @return Возвращает список идентификаторов доменных обектов типа созданных из писем
     */
    List<Id> receive(EmailReceiverConfig config) throws FatalException;
}
