package ru.intertrust.cm.core.business.impl.email;

import java.util.List;

import javax.ejb.SessionContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.EmailReceiver;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.email.EmailReceiverConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.schedule.SheduleType;
import ru.intertrust.cm.core.model.ScheduleException;

@ScheduleTask(name = "EmailReceiverScheduleTask", minute = "*/1", type = SheduleType.Multipliable)
public class EmailReceiverScheduleTaskHandle implements ScheduleTaskHandle {

    @Autowired
    private EmailReceiver emailReceiver;

    @Override
    public String execute(SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        try {
            List<Id> messages = emailReceiver.receive((EmailReceiverConfig) parameters);
            return "Receive " + messages.size() + " messages";
        } catch (Exception ex) {
            throw new ScheduleException("Error exec EmailReceiverScheduleTaskHandle", ex);
        }
    }

}
