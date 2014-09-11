package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;

/**
 * @author Sergey.Okolot
 *         Created on 11.09.2014 14:14.
 */
public final class ExceptionMessageFactory {

    public static String getMessage(final Command command, Throwable ex) {
        if (ex instanceof GuiException) {
            if (ex.getCause() == null) {
                return ex.getMessage();
            }
            ex = ex.getCause();
            switch(ex.getClass().getSimpleName()) {
                case "AccessException":
                    return "У вас нет прав доступа к даному объекту.";
                case "ActionServiceException":
                    return "Невозможно получить список действий для объекта.";
                case "AuthenticationException":
                    return "Ошибка авторизации.";
                case "CollectionConfigurationException":
                    return "Ошибка конфигурации коллекции.";
                case "CollectionQueryException":
                    return "Ошибка в SQL запросе.";
                case "ConfigurationException":
                    return "Ошибка конфигурации объекта.";
                case "CrudException":
                    return "Ошибка операции с БД.";
                case "DaoException":
                    return "Ошибка операции с БД.";
                case "DoelException":
                    return "Ошибка обработки DOEL выражения.";
                case "DoelParseException":
                    return "Ошибка разбора DOEL выражения.";
                case "EventTriggerException":
                    return "Ошибка инициализации тригера.";
                case "ExtensionPointException":
                    return "Ошибка инициализации точки расширения.";
                case "FatalException":
                    return "Фатальная ошибка приложения.";
//                case "InboxNotificationException": не используется
                case "InvalidIdException":
                    return "Некоректный идентификатор ДО.";
                case "MailNotificationException":
                    return "Ошибка отправки/приема сообщения по email.";
                case "NotificationException":
                    return "Ошибка отправки сообщения.";
                case "ObjectNotFoundException":
                    return "Данные не найдено.";
                case "OptimisticLockException":
                    return "Сохранение невозможно, данные изменены другим пользователем.";
                case "PermissionException":
                    return "Ошибка создания группы.";
                case "ProcessException":
                    return "Ошибка выполнения workflow процесса.";
                case "ProfileException":
                    return "Ошибка обработки профиля системы/пользователя.";
                case "ReportServiceException":
                    return "Ошибка обработки отчета";
                case "ScheduleException":
                    return "Ошибка работы подсистемы периодических заданий";
                case "SearchException":
                    return "Ошибка работы подсистемы поиска";
                case "UnexpectedException":
                    return "Неизвестная ошибка приложения.";
//                case "ValidationException": Сообщения обрабатываются самостоятельно
            }
        }
        return  "Системная ошибка при выполнении '" + command.getName() + "', обратитесь к администратору.";
    }
}
