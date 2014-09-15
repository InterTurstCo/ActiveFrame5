package ru.intertrust.cm.core.dao.impl;

import org.springframework.stereotype.Service;

import ru.intertrust.cm.core.dao.api.ActionListener;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.exception.DaoException;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Vlad Simonenko
 * Реализация сервиса для работы с пользовательскими транзакциями.
 * Сервис регистрирует слушателя, который должен быть выполнен по завершению транзакции.
 * Если действия выполняются вне контеста контейнерной транзакции,
 * регистрация слушателей будет проигнорированна.
 */
@Service
public class UserTransactionServiceImpl implements UserTransactionService{

    @Resource
    private TransactionSynchronizationRegistry txReg;

    private TransactionSynchronizationRegistry getTxReg() {
        if (txReg == null) {
            try {
                txReg = (TransactionSynchronizationRegistry) new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            } catch (NamingException e) {
                throw new DaoException(e);
            }
        }
        return txReg;
    }

    /**
     * Регистрируем событие для фиксации или отката "пользовательской" транзакции.
     * @param actionListener - событие, необходимо переопределить методы onCommit и onRollback.
     */
    @Override
    public void addListener(ActionListener actionListener) {
        //не обрабатываем вне транзакции
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        List actionListeners = (List) getTxReg().getResource(ListenerBasedSynchronization.class);
        if (actionListeners == null) {
            actionListeners = new ArrayList();
            getTxReg().putResource(ListenerBasedSynchronization.class, actionListeners);
            getTxReg().registerInterposedSynchronization(new ListenerBasedSynchronization(actionListeners));
        }
        actionListeners.add(actionListener);
    }

    @Override
    public String getTransactionId() {
        Object transactionKey = getTxReg().getTransactionKey();
        return transactionKey == null ? null : transactionKey.toString();
    }

    @Override
    public <T> T getListener(Class<T> tClass){
        //не обрабатываем вне транзакции
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        List actionListeners = (List) getTxReg().getResource(ListenerBasedSynchronization.class);

        if (actionListeners == null) {
            return null;
        }

        for (Object l : actionListeners) {
            if (tClass.equals(l.getClass())){
                return (T) l;
            }
        }

        return null;
    }

    /**
     * Специализированный метод регистрации события фиксации или отката транзакции
     * для работы с файлом. В случае "отката" файл будет удален из файловой системы,
     * если он уже был там создан.
     * @param filePath - полный путь к файлу.
     */
    @Override
    public void addListenerForSaveFile(final String filePath) {
        addListener(new ActionListener() {
            @Override
            public void onCommit() {
            }

            @Override
            public void onRollback() {
                File f = new File(filePath);
                if (f.exists()) {
                    try {
                        f.delete();
                    } catch (RuntimeException ex) {
                    }
                }
            }
        });
    }

    static private class ListenerBasedSynchronization implements Synchronization {
        List<ActionListener> actionListeners;

        public ListenerBasedSynchronization(List list) {
            this.actionListeners = list;
        }

        @Override
        public void beforeCompletion() {
            //Идем с конца спсика, чтобы не получить ошибку модификации списка в итераторе
            //for (ActionListener l : actionListeners) {
            for (int i=actionListeners.size()-1; i>=0; i--){
                actionListeners.get(i).onCommit();
            }
        }

        @Override
        public void afterCompletion(int status) {
            try {
                if (Status.STATUS_ROLLEDBACK == status) {
                    //for (ActionListener l : actionListeners) {
                    for (int i=actionListeners.size()-1; i>=0; i--){
                        actionListeners.get(i).onRollback();
                    }
                }
            } finally {
                actionListeners = null;
            }
        }
    }
}