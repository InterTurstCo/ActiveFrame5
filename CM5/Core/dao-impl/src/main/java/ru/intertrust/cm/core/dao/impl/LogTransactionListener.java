package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.ActionListener;

import java.util.ArrayList;
import java.util.List;

public class LogTransactionListener implements ActionListener {


    private long startTime;
    private long minTime;
    private String transactionId;
    private List<String> logEntries = new ArrayList<>();

    public LogTransactionListener(long startTime, String transactionId, long minTime) {
        this.startTime = startTime;
        this.minTime = minTime;
        this.transactionId = transactionId;
    }

    @Override
    public void onCommit() {
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
        long delay = System.currentTimeMillis() - startTime;
        if (delay > minTime) {
            SqlTransactionLogger.logTransactionTrace(this, isCommitted, delay);
        }
    }


}
