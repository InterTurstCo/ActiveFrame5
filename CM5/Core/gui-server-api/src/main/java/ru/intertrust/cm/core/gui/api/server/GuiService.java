package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;

import javax.ejb.SessionContext;
import java.util.HashSet;

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
    Dto executeCommand(Command command, UserInfo userInfo) throws GuiException;

    FormDisplayData getForm(String domainObjectType, UserInfo userInfo);

    FormDisplayData getForm(Id domainObjectId, UserInfo userInfo);

    // получение формы расширенного поиска
    FormDisplayData getSearchForm(String domainObjectType, HashSet<String> formFields, UserInfo userInfo);

    DomainObject saveForm(FormState formState, UserInfo userInfo);

    SessionContext getSessionContext();

    FormConfig getFormConfig(String typeName, boolean isSearchForm);

}
