package ru.intertrust.cm.core.business.api;

import java.util.List;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;

/**
 * Сервис отправки уведомлений.
 * @author larin
 * 
 */
public interface NotificationService {

    /**
     * Remote интерфейс
     * @author larin
     * 
     */
    public interface Remote extends NotificationService {
    }

    /**
     * Отправка уведомления после успешного завершения транзакции. Метод
     * вызывается бизнес методами во время открытой транзакции. Реальная
     * отправка происходит после удачной завершенной транзакции
     * @param notificationType
     *            Тип уведомления
     * @param sender
     *            Идентификатор персоны отправителя, может быть nullБ в этом
     *            случае подставится текущая персона
     * @param addresseeList
     *            список адресатов. Может быть персона, группа, динамическая
     *            группа или контекстная роль
     * @param priority
     *            приоритет сообщения. Влияет на отображение данного сообщения,
     *            но не влияет на очередность отправки
     * @param context
     *            Контекст сообщения. Содержит информацию о объекте системы,
     *            относительно которой производится отправка уведомления
     */
    void sendOnTransactionSuccess(String notificationType, Id sender, List<NotificationAddressee> addresseeList, NotificationPriority priority,
            NotificationContext context);

    /**
     * Отправка уведомления после успешного завершения транзакции. Метод
     * вызывается бизнес методами во время открытой транзакции. Реальная
     * отправка происходит после удачной завершенной транзакции
     * @param notificationType
     *            Тип уведомления
     * @param senderName
     *            Отображаемое имя отправителя, может быть null, в этом случае
     *            подставится текущая персона
     * @param addresseeList
     *            список адресатов. Может быть персона, группа, динамическая
     *            группа или контекстная роль
     * @param priority
     *            приоритет сообщения. Влияет на отображение данного сообщения,
     *            но не влияет на очередность отправки
     * @param context
     *            Контекст сообщения. Содержит информацию о объекте системы,
     *            относительно которой производится отправка уведомления
     */
    void sendOnTransactionSuccess(String notificationType, String senderName, List<NotificationAddressee> addresseeList, NotificationPriority priority,
            NotificationContext context);

    /**
     * Метод асинхронной отправки уведомления. Используется из метода
     * sendOnTransactionSuccess. Отправка производится независимо от результата
     * транзакции.
     * @param notificationType
     *            тип сообщения
     * @param sender
     *            идентификатор персоны отправителя
     * @param addresseeList
     *            список адресатов
     * @param priority
     *            приоритет
     * @param context
     *            контекст сообщения
     * @return
     */
    Future<Boolean> sendNow(String notificationType, Id sender, List<NotificationAddressee> addresseeList,
            NotificationPriority priority,
            NotificationContext context);

    /**
     * Метод асинхронной отправки уведомления. Используется из метода
     * sendOnTransactionSuccess. Отправка производится независимо от результата
     * транзакции.
     * @param notificationType
     *            тип сообщения
     * @param senderName
     *            Отображаемое имя отправителя
     * @param addresseeList
     *            список адресатов
     * @param priority
     *            приоритет
     * @param context
     *            контекст сообщения
     * @return
     */
    Future<Boolean> sendNow(String notificationType, String senderName, List<NotificationAddressee> addresseeList,
            NotificationPriority priority,
            NotificationContext context);

    /**
     * Метод синхронной отправки уведомлений в том же потоке. Используется когда
     * уже запущен служебный поток например из триггеров или периодических
     * заданий
     * @param notificationType
     *            тип сообщения
     * @param sender
     *            идентификатор персоны отправителя
     * @param addresseeList
     *            список адресатов
     * @param priority
     *            приоритет
     * @param context
     *            контекст сообщения
     * @param context
     */
    void sendSync(String notificationType, Id sender, List<NotificationAddressee> addresseeList,
            NotificationPriority priority,
            NotificationContext context);

    /**
     * Метод синхронной отправки уведомлений в том же потоке. Используется когда
     * уже запущен служебный поток например из триггеров или периодических
     * заданий
     * @param notificationType
     *            тип сообщения
     * @param senderName
     *            Отображаемое имя отправителя
     * @param addresseeList
     *            список адресатов
     * @param priority
     *            приоритет
     * @param context
     *            контекст сообщения
     * @param context
     */
    void sendSync(String notificationType, String senderName, List<NotificationAddressee> addresseeList,
            NotificationPriority priority,
            NotificationContext context);
}
