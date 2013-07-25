package ru.intertrust.cm.core.business.impl;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.model.FatalException;

import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * @author vmatsukevich
 *         Date: 7/26/13
 *         Time: 1:04 PM
 */
@Aspect
public class AttachmentCleaner {

    @Autowired
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    //@Before("execution(* ru.intertrust.cm.core.business.impl.CrudServiceImpl.save(..))")
    public void setTransactionCallback() {
        if (transactionSynchronizationRegistry.getTransactionKey() == null) {
            throw new FatalException("Attachment stuff is invoked outside transaction.");
        }

        transactionSynchronizationRegistry.registerInterposedSynchronization(new AttachmentCleanerCallback());
    }

    private class AttachmentCleanerCallback implements Synchronization {

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(int i) {
            //TODO: очистить мусор вложений
        }

    }
}
