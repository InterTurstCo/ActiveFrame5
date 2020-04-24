package ru.intertrust.cm.core.dao.impl;

public interface DatabaseDaoFactory {
    
    /**
     * Возвращает признак "DDL-транзакции поддерживаются".
     */
    boolean isDdlTransactionsSupports();

}