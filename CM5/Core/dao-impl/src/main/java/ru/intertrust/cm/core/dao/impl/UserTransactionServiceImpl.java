package ru.intertrust.cm.core.dao.impl;

import org.springframework.stereotype.Service;
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
public class UserTransactionServiceImpl {

    @Resource
    private TransactionSynchronizationRegistry txReg;

    /**
     * Абстракция слушателя, методы которого будут вызванны
     * при выполнении commit или rollback контейнерной транзакции.
     */
    static abstract interface ActionListener {
        /**
         * метод вызывается до запуска двухфазного процесса фиксации транзакции.
         * Этот вызов выполняется в контексте контейнерной транзакции, которая фиксируется.
         */
        void onCommit();

        /**
         * метод вызывается после того, как транзакция фиксируется или откатывается.
         * Этот вызов выполняется вне контекста контейнерной транзакции.
         */
        void onRollback();
    }

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
    public void addListener(ActionListener actionListener) {
        //не обрабатываем вне транзакции
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        List actionListeners = (List) getTxReg().getResource(ListenerBasedSynchronization.class);
        if (actionListeners == null) {
            actionListeners = new ArrayList();
            getTxReg().putResource(Synchronization.class, actionListeners);
            getTxReg().registerInterposedSynchronization(new ListenerBasedSynchronization(actionListeners));
        }
        actionListeners.add(actionListener);
    }

    /**
     * Специализированный метод регистрации события фиксации или отката транзакции
     * для работы с файлом. В случае "отката" файл будет удален из файловой системы,
     * если он уже был там создан.
     * @param filePath - полный путь к файлу.
     */
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
            for (ActionListener l : actionListeners) {
                l.onCommit();
            }
        }

        @Override
        public void afterCompletion(int status) {
            try {
                if (Status.STATUS_ROLLEDBACK == status) {
                    for (ActionListener l : actionListeners) {
                        l.onRollback();
                    }
                }
            } finally {
                actionListeners = null;
            }
        }
    }
}