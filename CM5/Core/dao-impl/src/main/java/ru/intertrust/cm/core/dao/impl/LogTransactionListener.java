package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.ActionListener;

import java.util.ArrayList;
import java.util.List;

public class LogTransactionListener implements ActionListener {


    private long startTime;
    private long minTime;
    private String transactionId;
    private List<String> logEntries = new ArrayList<>();
    private Long preparationTime = new Long(0);

    public LogTransactionListener(long startTime, String transactionId, long minTime) {
        this.startTime = startTime;
        this.minTime = minTime;
        this.transactionId = transactionId;
    }

    @Override
    public void onBeforeCommit() {
        printTransactionTrace(true);
    }

    @Override
    public void onRollback() {
        printTransactionTrace(false);
    }

    public void addSqlLogEntry(String logEntry) {
        logEntries.add(logEntry);
    }

    String getTransactionId() {
        return transactionId;
    }

    List<String> getLogEntries(){
        return logEntries;
    }

    private void printTransactionTrace(boolean isCommitted) {
        long endTime = System.currentTimeMillis();
        long delay = endTime - startTime;
        
        
        if (((delay > minTime) && SqlTransactionLogger.isDebugEnabled()) || SqlTransactionLogger.isTraceEnabled()) {
            SqlTransactionLogger.logTransactionTrace(this, isCommitted, startTime, endTime);
        }
    }

    @Override
    public void onAfterCommit() {
        //Ничего не делаем
        
    }

    public void addPreparationTime(Long preparationTime) {
        this.preparationTime += preparationTime;
    }

    public Long getPreparationTime() {
        return preparationTime;
    }

}
