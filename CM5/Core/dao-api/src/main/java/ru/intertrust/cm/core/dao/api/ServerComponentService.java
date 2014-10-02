package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.dao.api.component.ServerComponentHandler;

/**
 * Сервис для инициализации и получения серверных компонентов по имени. 
 * @author atsvetkov
 *
 */
public interface ServerComponentService {

    ServerComponentHandler getServerComponent(String componentName);

}
