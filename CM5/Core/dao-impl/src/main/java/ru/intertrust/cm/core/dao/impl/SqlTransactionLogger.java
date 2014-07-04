package ru.intertrust.cm.core.dao.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlTransactionLogger {

    private static final Logger logger = LoggerFactory.getLogger(SqlTransactionLogger.class);


    public static synchronized void logTransactionTrace(LogTransactionListener transactionListener, boolean isCommitted, long delay) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatTransactionTrace(transactionListener, isCommitted, delay));
        } else if (logger.isTraceEnabled()){
            logger.trace(formatTransactionTrace(transactionListener, isCommitted, delay));
        }
    }

    private static String formatTransactionTrace(LogTransactionListener transactionListener, boolean isCommitted, long delay) {
        StringBuilder result = new StringBuilder();
        String transactionId = transactionListener.getTransactionId();
        result.append("\n----- Transaction ").append(transactionId).append(isCommitted ? " committed" : " rolled back");
        result.append(" in ").append(delay).append(" ms -----\n");

        for (String logEntry : transactionListener.getLogEntries()) {
            result.append(logEntry).append("\n");
        }
        result.append("----- End of Transaction ").append(transactionId).append(" details -----");

        return result.toString();
    }

}
