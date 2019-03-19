package ru.intertrust.cm.core.dao.impl;


import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

public class SqlTransactionLogger {

    private static final String TIME_PATTERN = "HH:mm:ss.SSS";
    private static final Logger logger = LoggerFactory.getLogger(SqlTransactionLogger.class);

    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public static boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public static synchronized void logTransactionTrace(LogTransactionListener transactionListener, boolean isCommitted, long startTime, long endTime) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatTransactionTrace(transactionListener, isCommitted, startTime, endTime));
        } else if (logger.isTraceEnabled()){
            logger.trace(formatTransactionTrace(transactionListener, isCommitted, startTime, endTime));
        }
    }

    private static String formatTransactionTrace(LogTransactionListener transactionListener, boolean isCommitted, long startTime, long endTime) {
        long delay = endTime - startTime;
        StringBuilder timeInterval = new StringBuilder();        
        timeInterval.append(ThreadSafeDateFormat.format(new Date(startTime), TIME_PATTERN)).append(" - ").append(ThreadSafeDateFormat.format(new Date(endTime), TIME_PATTERN));
        
        StringBuilder result = new StringBuilder();
        String transactionId = transactionListener.getTransactionId();
        result.append("\n----- Transaction ").append(transactionId).append(isCommitted ? " committed" : " rolled back");
        result.append(" in ").append(delay).append(" ms (").append(timeInterval).append(")-----\n");
        
        result.append("----- SQL query total preparation time:  ").append(getTimeInMilliseconds(transactionListener.getPreparationTime())).append(" ms-----\n");
        
        for (String logEntry : transactionListener.getLogEntries()) {
            result.append(logEntry).append("\n");
        }
        result.append("----- End of Transaction ").append(transactionId).append(" details -----");

        return result.toString();
    }
    
    private static long getTimeInMilliseconds(Long nanoTime) {
        return Math.round(nanoTime / 1000000d);
    }

}
