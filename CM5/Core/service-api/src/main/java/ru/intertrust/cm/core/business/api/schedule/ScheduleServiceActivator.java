package ru.intertrust.cm.core.business.api.schedule;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Спринг бин для активации сервиса периодических заданий.
 * Каждое приложение по своему усмотрению актифирует сервис периодических заданий. Достаточно добавить этот бин в нужный spring контекст
 * По умолчанию сервис периодических заданий отключен
 * @author larin
 *
 */
public class ScheduleServiceActivator {
    @Autowired
    private ScheduleTaskLoader scheduleTaskLoader;
    
    @PostConstruct
    public void startScheduleService(){
        scheduleTaskLoader.setEnable(true);
    }
}
