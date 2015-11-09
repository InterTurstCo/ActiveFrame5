package ru.intertrust.testmodule.url;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.NotificationTextFormer;
import ru.intertrust.cm.core.business.api.ThreadContext;

/**
 * Бин для тестирования thread переменных
 * @author larin
 *
 */
public class TestHashFormer {
    @Autowired
    private ThreadContext threadContext;
    
    public String getHash(){
        String result = threadContext.get(NotificationTextFormer.PARAM_NOTIFICATION_TYPE) + ":";
        result += threadContext.get(NotificationTextFormer.PARAM_NOTIFICATION_PART) + ":";
        result += threadContext.get(NotificationTextFormer.PARAM_NOTIFICATION_ADDRESSEE) + ":";
        result += threadContext.get(NotificationTextFormer.PARAM_NOTIFICATION_LOCALE) + ":";
        result += threadContext.get(NotificationTextFormer.PARAM_NOTIFICATION_CHANNEL) + ":";
        result += threadContext.get(NotificationTextFormer.PARAM_NOTIFICATION_CONTEXT);
        
        return String.valueOf(result.hashCode());
    }
}
