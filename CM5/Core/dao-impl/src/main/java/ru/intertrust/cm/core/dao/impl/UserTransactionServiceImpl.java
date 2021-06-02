package ru.intertrust.cm.core.dao.impl;

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
import java.util.Collections;
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
            return; // не обрабатываем вне транзакции
        }
        
        List<ActionListener> actionListeners = (List<ActionListener>)this.getTxReg().getResource(ListenerBasedSynchronization.class);
        
        if (actionListeners == null) {
            this.getTxReg().putResource(ListenerBasedSynchronization.class, actionListeners = new ArrayList<>());
            this.getTxReg().registerInterposedSynchronization(new ListenerBasedSynchronization(actionListeners, this.currentDataSourceContext));
        }
        
        actionListeners.add(actionListener);
        
    }

    @Override
    public <T> T getListener (final Class<T> clazz){
        
        if (this.getTransactionId() == null) {
            return null; // не обрабатываем вне транзакции
        }

        final List<?> actionListeners = (List<?>)this.getTxReg().getResource(ListenerBasedSynchronization.class);
        T result = null;
        
        if (actionListeners != null) {
            for (final Object listener : actionListeners) {
                if (clazz.equals(listener.getClass())) {
                    if (result == null) {
                        result = clazz.cast(listener);
                    } else {
                        throw new RuntimeException("More than 1 listeners registered for '" + clazz + "'");
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
        
        private final List<ActionListener> actionListeners;
        private final CurrentDataSourceContext currentDataSourceContext;

        public ListenerBasedSynchronization (final List<ActionListener> actionListeners, final CurrentDataSourceContext currentDataSourceContext) {
            this.actionListeners = actionListeners;
            this.currentDataSourceContext = currentDataSourceContext;
        }

        @Override
        public void beforeCompletion () {
            this.currentDataSourceContext.reset(); // устанавливаем источник данных в МАСТЕР
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