package ru.inttrust.workflow.runawfe.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Session{
    private static final Log log = LogFactory.getLog(Session.class);

    private ExecutionContext executionContext;
    private SwimlaneDao swimlaneDao;
    private ExecutorDao executorDao;


    public Session(ExecutionContext executionContext, SwimlaneDao swimlaneDao, ExecutorDao executorDao){
        this.executionContext = executionContext;
        this.swimlaneDao = swimlaneDao;
        this.executorDao = executorDao;
    }

    /**
     * Установка пользователей в роль
     * @param roleName
     * @param executorNames
     */
    public void setRole(String roleName, String ... executorNames) {
        SwimlaneDefinition swimlaneDefinition = executionContext.getProcessDefinition().getSwimlaneNotNull(roleName);
        Swimlane swimlane = swimlaneDao.findOrCreate(executionContext.getProcess(), swimlaneDefinition);

        List<Executor> executors = new ArrayList<>();
        for (int i = 0; i < executorNames.length; i++) {
            executors.add(executorDao.getExecutor(executorNames[i]));
        }
        AssignmentHelper.assign(executionContext, swimlane, executors);
    }

    /**
     * Получение исполнителя по имени
     * @param name
     * @return
     */
    public Executor getExecutor(String name){
        return executorDao.getExecutor(name);
    }

    /**
     * Вывод информации в лог сервера с уровнем info
     * @param message
     */
    public void info(Object message){
        log.info(message);
    }

    /**
     * Вывод информации в лог сервера с уровнем debug
     * @param message
     */
    public void debug(Object message){
        log.debug(message);
    }
}
