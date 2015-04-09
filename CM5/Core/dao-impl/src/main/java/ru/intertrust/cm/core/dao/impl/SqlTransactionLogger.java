package ru.intertrust.cm.core.dao.impl;


import java.util.Date;
import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlTransactionLogger {

    private static final Logger logger = LoggerFactory.getLogger(SqlTransactionLogger.class);


    public static synchronized void logTransactionTrace(LogTransactionListener transactionListener, boolean isCommitted, long startTime, long endTime) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatTransactionTrace(transactionListener, isCommitted, startTime, endTime));
        } else if (logger.isTraceEnabled()){
            logger.trace(formatTransactionTrace(transactionListener, isCommitted, startTime, endTime));
        }
    }

    private static String formatTransactionTrace(LogTransactionListener transactionListener, boolean isCommitted, long startTime, long endTime) {
        long delay = endTime - startTime;
        StringBuilder timeInterval = new StringBuilder();        
        Formatter formatter = new Formatter(timeInterval);
        String format = "(%1$TH.%1$TM.%1$TS:%1$TL - %2$TH.%2$TM.%2$TS:%2$TL) ";
        formatter.format(format, new Date(startTime), new Date(endTime));

        StringBuilder result = new StringBuilder();
        String transactionId = transactionListener.getTransactionId();
        result.append("\n----- Transaction ").append(transactionId).append(isCommitted ? " committed" : " rolled back");
        result.append(" in ").append(delay).append(" ms ").append(timeInterval).append("-----\n");
        
        result.append("-----SQL query total preparation time:  ").append(getTimeInMilliseconds(transactionListener.getPreparationTime())).append(" ms-----\n");
        
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
