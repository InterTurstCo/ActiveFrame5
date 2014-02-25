package ru.intertrust.cm.core.business.api;

import java.net.URL;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис формирования url ссылок
 * @author larin
 * 
 */
public interface UrlFormer {

    /**
     * Формирует url к объекту системы. Формирование url производится в контексте определенного клиента платформы.
     * @param clientName
     *            имя клиента
     * @param addressee
     *            идентификатор персоны адресата
     * @param objectId
     *            идентификатор доменного объекта
     * @return
     */
    URL getUrl(String clientName, Id addressee, Id objectId);
}
