package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.intertrust.cm.core.business.api.notification.NotificationChannel;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelHandle;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelInfo;
import ru.intertrust.cm.core.business.api.notification.NotificationChannelLoader;
import ru.intertrust.cm.core.dao.api.ClassPathScanService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Имплементация сервиса поиска каналов отправки уведомлений
 *
 * @author larin
 */
public class NotificationChannelLoaderImpl implements NotificationChannelLoader, ApplicationContextAware {
    @Autowired
    private ClassPathScanService scanner;

    private ApplicationContext applicationContext;

    private Map<String, NotificationChannelInfo> channels = new Hashtable<String, NotificationChannelInfo>();

    @Override
    public List<String> getNotificationChannelNames() {
        List<String> result = new ArrayList<String>();
        Set<String> keys = channels.keySet();
        for (String key : keys) {
            result.add(key);
        }
        return result;
    }

    @Override
    public NotificationChannelHandle getNotificationChannel(String channelName) {
        return channels.get(channelName).getChannel();
    }

    public void init() throws ClassNotFoundException {

        //Цикл по найденным классам
        for (BeanDefinition bd : scanner.findClassesByAnnotation(NotificationChannel.class)) {
            String className = bd.getBeanClassName();

            // Получение найденного класса
            Class<?> channelClass = Class.forName(className);

            // Получение анотации NotificationChannel
            NotificationChannel annatation = (NotificationChannel) channelClass
                    .getAnnotation(NotificationChannel.class);

            // Проверка наличия анотации в классе
            if (annatation != null) {

                // Проверка наличия анотации в классе
                if (NotificationChannelHandle.class.isAssignableFrom(channelClass)) {

                    // Создаем экземпляр точки расширения
                    // Добавляем
                    // класс как спринговый бин с поддержкой
                    // autowire
                    NotificationChannelHandle channel = (NotificationChannelHandle) applicationContext
                            .getAutowireCapableBeanFactory().createBean(channelClass,
                                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
                    channels.put(annatation.name(),
                            new NotificationChannelInfo(annatation.name(), annatation.description(), channel));
                }
            }
        }
    }

    /**
     * Установка spring контекста
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public NotificationChannelInfo getNotificationChannelInfo(String channelName) {
        return channels.get(channelName);
    }
}
