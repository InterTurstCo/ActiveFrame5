package ru.intertrust.cm.core.service.it.notification;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.notification.NotificationChannel;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;

@NotificationChannel(name = "TEST_NOTIFICATION_CHANNEL",
        description = "Тестовый канал для использования в интеграционных тестах")
public class NotificationTestChannel implements NotificationChannelHandle {
    private static List<NotificationItem> notifications = new ArrayList<NotificationItem>(); 
    
    @Override
    public void send(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context) {
        synchronized (notifications) {
            notifications.add(new NotificationItem(notificationType, senderId, addresseeId, priority, context));
        }
    }

    public void clear(){
        synchronized (notifications) {
            notifications.clear();
        }
    }
    
    public boolean contains(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
            NotificationContext context){
        NotificationItem comparedItem = new NotificationItem(notificationType, senderId, addresseeId, priority, context);
        return notifications.contains(comparedItem);
    }
    
    public class NotificationItem {
        private String notificationType;
        private Id senderId;
        private Id addresseeId;
        private NotificationPriority priority;
        private NotificationContext context;
        
        private NotificationItem(String notificationType, Id senderId, Id addresseeId, NotificationPriority priority,
                NotificationContext context){
            this.notificationType = notificationType;
            this.senderId = senderId;
            this.addresseeId = addresseeId;
            this.priority = priority;
            this.context = context;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((addresseeId == null) ? 0 : addresseeId.hashCode());
            result = prime * result + ((context == null) ? 0 : context.hashCode());
            result = prime * result + ((notificationType == null) ? 0 : notificationType.hashCode());
            result = prime * result + ((priority == null) ? 0 : priority.hashCode());
            result = prime * result + ((senderId == null) ? 0 : senderId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NotificationItem other = (NotificationItem) obj;
            if (addresseeId == null) {
                if (other.addresseeId != null)
                    return false;
            } else if (!addresseeId.equals(other.addresseeId))
                return false;
            if (context == null) {
                if (other.context != null)
                    return false;
            } else if (!context.equals(other.context))
                return false;
            if (notificationType == null) {
                if (other.notificationType != null)
                    return false;
            } else if (!notificationType.equals(other.notificationType))
                return false;
            if (priority != other.priority)
                return false;
            if (senderId == null) {
                if (other.senderId != null)
                    return false;
            } else if (!senderId.equals(other.senderId))
                return false;
            return true;
        }
        
    }

}
