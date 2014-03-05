package ru.intertrust.cm.core.process;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
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
    private Expression addressee;
    private Expression context;
    private Expression notificationType;
    private Expression notificationPriority;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IdService idService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        NotificationAddresseeConverter converter = (NotificationAddresseeConverter) addressee.getValue(execution);
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.addContextObject("document", (Id) context.getValue(execution));
        notificationService.sendOnTransactionSuccess(
                (String) notificationType.getValue(execution),
                null,
                converter.getAddresseeList(),
                NotificationPriority.valueOf((String) notificationPriority.getValue(execution)),
                notificationContext);
    }

}
