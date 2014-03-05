package ru.intertrust.cm.core.workflow.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.tools.NotificationAddresseeConverter;
import ru.intertrust.cm.core.tools.SpringClient;

/**
 * Класс для использования в процессах для отправки уведомления
 * @author larin
 * 
 */
public class SendNotification extends SpringClient implements JavaDelegate {
    private String addressee;
    private Id context;
    private String notificationType;
    private String notificationPriority;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IdService idService;

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public Id getContext() {
        return context;
    }

    public void setContext(Id context) {
        this.context = context;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationPriority() {
        return notificationPriority;
    }

    public void setNotificationPriority(String notificationPriority) {
        this.notificationPriority = notificationPriority;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        NotificationAddresseeConverter converter = NotificationAddresseeConverter.load(addressee);
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.addContextObject("document", this.context);
        notificationService.sendOnTransactionSuccess(notificationType, null, converter.getAddresseeList(),
                NotificationPriority.valueOf(notificationPriority), notificationContext);
    }

}
