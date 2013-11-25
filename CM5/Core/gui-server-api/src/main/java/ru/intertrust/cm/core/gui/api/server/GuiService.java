package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;

import javax.ejb.SessionContext;

/**
 * Данный класс-служба содержит операции, относящиеся к клиентскому приложению. Клиентское приложение может быть
 * сконфигурировано по-разному для различных ролей или конечных пользователей. Все методы данной службы учитывают
 * данный факт прозрачным образом, получая информацию о текущем пользователе (и его роли) из контекста выполнения.
 *
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:12
 */
public interface GuiService {
    public interface Remote extends GuiService {
    }

    /**
     * Возвращает конфигурацию панели навигации.
     * @return конфигурацию панели навигации
     */
    NavigationConfig getNavigationConfiguration();

    /**
     * Выполняет команду компонента GUI (например, плагина или виджета) и возвращает результат
     * @param command команда плагина
     * @return результат выполнения команды
     */
    Dto executeCommand(Command command) throws GuiException;

    FormDisplayData getForm(String domainObjectType);

    FormDisplayData getForm(Id domainObjectId);

    DomainObject saveForm(FormState formState);

    public SessionContext getSessionContext();

}
