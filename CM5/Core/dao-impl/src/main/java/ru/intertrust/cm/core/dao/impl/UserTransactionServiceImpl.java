package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.dao.api.ActionListener;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.dao.api.UserTransactionService;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.dao.impl.access.DynamicGroupServiceImpl;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Реализация сервиса для работы с пользовательскими транзакциями.
 * Сервис регистрирует слушателя, который должен быть выполнен по завершению транзакции.
 * Если действия выполняются вне контеста контейнерной транзакции, регистрация слушателей будет проигнорирована.
 * @author Vlad Simonenko, Gleb Nozdrachev
*/
public class UserTransactionServiceImpl implements UserTransactionService{

    private static final Logger log = LoggerFactory.getLogger(UserTransactionServiceImpl.class);

    @Resource
    private TransactionSynchronizationRegistry txReg;

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

    /**
     * Регистрируем событие для фиксации или отката "пользовательской" транзакции.
     * @param actionListener - событие, необходимо переопределить методы onCommit и onRollback.
     */
    @Override
    public void addListener (final ActionListener actionListener) {
        
        if (this.getTransactionId() == null) {
            return;
        }
        
        ListenerBasedSynchronization mainListener = (ListenerBasedSynchronization)this.getTxReg().getResource(ListenerBasedSynchronization.class);
        
        if (mainListener == null) {
            this.getTxReg().putResource(ListenerBasedSynchronization.class, mainListener = new ListenerBasedSynchronization(this.currentDataSourceContext));
            this.getTxReg().registerInterposedSynchronization(mainListener);
        }
        
        if (mainListener.actionListenersCheck.add(actionListener)) {
            mainListener.actionListeners.add(actionListener);
            log.debug("UserTransactionServiceImpl / addListener: actionListener = {}", actionListener);
        }
        
    }

    @Override
    public <T> T getListener (final Class<T> clazz){
        
        if (this.getTransactionId() == null) {
            return null;
        }

        final ListenerBasedSynchronization mainListener = (ListenerBasedSynchronization)this.getTxReg().getResource(ListenerBasedSynchronization.class);
        T result = null;
        
        if (mainListener != null) {
            for (final Object actionListener : mainListener.actionListeners) {
                if (clazz.equals(actionListener.getClass())) {
                    if (result == null) {
                        result = clazz.cast(actionListener);
                    } else {
                        throw new RuntimeException("More than 1 action-listeners registered for '" + clazz + "'");
                    }
                }
            }
        }
        
        return result;
        
    }

    /**
     * А.П. - Реализация перенесена в {@link FileSystemAttachmentContentDaoImpl}.
     */
    @Override
    public void addListenerForSaveFile (final String filePath) {
    }

    @Override
    public String getTransactionId () {
        final Object transactionKey = this.getTxReg().getTransactionKey();
        return transactionKey == null ? null : transactionKey.toString();
    }

    private TransactionSynchronizationRegistry getTxReg () {
        
        if (this.txReg == null) {
            try {
                this.txReg = (TransactionSynchronizationRegistry)(new InitialContext()).lookup("java:comp/TransactionSynchronizationRegistry");
            } catch (final NamingException e) {
                throw new DaoException(e);
            }
        }
        
        return this.txReg;
        
    }

    private static class ListenerBasedSynchronization implements Synchronization {
        
        private enum Operation {
            BeforeCommit, AfterCommit, Rollback
        }

        private static final Class<?>[] listenersFirst = new Class<?>[] {DomainObjectDaoImpl.CacheCommitNotifier.class};
        private static final Class<?>[] listenersLast  = new Class<?>[] {DynamicGroupServiceImpl.RecalcGroupSynchronization.class};
        
        private static final Set<Class<?>> listenersFirstAndLast = new HashSet<Class<?>>() {
            {
                this.addAll(Arrays.asList(listenersFirst));
                this.addAll(Arrays.asList(listenersLast));
            }
        };
        
        private final List<ActionListener> actionListeners = new ArrayList<>();
        private final Set<ActionListener> actionListenersCheck = new HashSet<>();
        private final CurrentDataSourceContext currentDataSourceContext;

        public ListenerBasedSynchronization (final CurrentDataSourceContext currentDataSourceContext) {
            this.currentDataSourceContext = currentDataSourceContext;
        }

        @Override
        public void beforeCompletion () {
            this.currentDataSourceContext.reset();
            this.notifyListeners(Operation.BeforeCommit);
        }

        @Override
        public void afterCompletion (final int status) {
            try {
                if (status == Status.STATUS_ROLLEDBACK) {
                    this.notifyListeners(Operation.Rollback);
                } else if (status == Status.STATUS_COMMITTED) {
                    this.notifyListeners(Operation.AfterCommit);
                }
            } finally {
                this.actionListeners.clear();
            }
        }
        
        private void notifyListeners (final Operation operation) {

            this.notifyListeners(listenersFirst, operation);

            for (int i = 0; i < this.actionListeners.size(); i++) {
                final ActionListener listener = this.actionListeners.get(i);
                if (!listenersFirstAndLast.contains(listener.getClass())) {
                    this.notifyListener(listener, operation);
                }
            }

            this.notifyListeners(listenersLast, operation);

        }
        
        private void notifyListeners (final Class<?>[] listeners, final Operation operation) {
            for (final Class<?> clazz : listeners) {
                for (int i = 0; i < this.actionListeners.size(); i++) {
                    final ActionListener listener = this.actionListeners.get(i);
                    if (clazz.equals(listener.getClass())) {
                        this.notifyListener(listener, operation);
                    }
                }
            }
        }
        
        private void notifyListener (final ActionListener listener, final Operation operation) {
            switch (operation) {
                case BeforeCommit:
                    listener.onBeforeCommit();
                    break;
                case AfterCommit:
                    listener.onAfterCommit();
                    break;
                case Rollback:
                    listener.onRollback();
                    break;
                default:
                    throw new RuntimeException("Unknown operation " + operation);
            }
        }
        
    }
    
}