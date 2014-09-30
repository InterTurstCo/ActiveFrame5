package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.model.SystemException;

/**
 * @author Sergey.Okolot
 *         Created on 11.09.2014 14:14.
 */
public final class ExceptionMessageFactory {

    private ExceptionMessageFactory() {
    }

    public static Pair<String, Boolean> getMessage(final Command command, Throwable ex) {
        SystemException cause = ex instanceof SystemException ? (SystemException) ex : null;
        while (ex.getCause() != null) {
            ex = ex.getCause();
            if (ex instanceof SystemException) {
                cause = (SystemException) ex;
            }
        }
        if (cause != null) {
            switch(cause.getClass().getSimpleName()) {
                case "AccessException":
                    return new Pair<>("У вас нет прав доступа к даному объекту.", false);
                case "ActionServiceException":
                    return new Pair<>("Невозможно получить список действий для объекта.", true);
                case "AuthenticationException":
                    return new Pair<>("Ошибка авторизации.", false);
                case "CollectionConfigurationException":
                    return new Pair<>("Ошибка конфигурации коллекции.", true);
                case "CollectionQueryException":
                    return new Pair<>("Ошибка в SQL запросе.", true);
                case "ConfigurationException":
                    return new Pair<>("Ошибка конфигурации объекта.", true);
                case "CrudException":
                    return new Pair<>("Ошибка операции с БД.", true);
                /*case "DaoException": parent exception for OptimisticLock and others
                    return new Pair<>("Ошибка операции с БД.", true);*/
                case "DoelException":
                    return new Pair<>("Ошибка обработки DOEL выражения.", true);
                case "DoelParseException":
                    return new Pair<>("Ошибка разбора DOEL выражения.", true);
                case "EventTriggerException":
                    return new Pair<>("Ошибка инициализации тригера.", true);
                case "ExtensionPointException":
                    return new Pair<>("Ошибка инициализации точки расширения.", true);
                case "FatalException":
                    return new Pair<>("Фатальная ошибка приложения.", true);
                case "GuiException" :
                    return new Pair<>(ex.getMessage(), true);
//                case "InboxNotificationException": не используется
                case "InvalidIdException":
                    return new Pair<>("Некоректный идентификатор ДО.", true);
                case "MailNotificationException":
                    return new Pair<>("Ошибка отправки/приема сообщения по email.", true);
                case "NotificationException":
                    return new Pair<>("Ошибка отправки сообщения.", true);
                case "ObjectNotFoundException":
                    return new Pair<>("Данных не найдено.", true);
                case "OptimisticLockException":
                    return new Pair<>("Сохранение невозможно, данные изменены другим пользователем.", false);
                case "PermissionException":
                    return new Pair<>("Ошибка создания группы.", false);
                case "ProcessException":
                    return new Pair<>("Ошибка выполнения workflow процесса.", true);
                case "ProfileException":
                    return new Pair<>("Ошибка обработки профиля системы/пользователя.", true);
                case "ReportServiceException":
                    return new Pair<>("Ошибка обработки отчета", true);
                case "ScheduleException":
                    return new Pair<>("Ошибка работы подсистемы периодических заданий", true);
                case "SearchException":
                    return new Pair<>("Ошибка работы подсистемы поиска", true);
                case "UnexpectedException":
                    return new Pair<>("Неизвестная ошибка приложения.", true);
                case "ValidationException":
                    return new Pair<>("Исправьте ошибки перед сохранением", true);
            }
        }
        return  new Pair<>("Системная ошибка при выполнении '" + command.getName() + "', обратитесь к администратору.", true);
    }
}
