package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Denis Mitavskiy
 *         Date: 31.01.2017
 *         Time: 17:44
 */
public class ResultSetExtractionLogger {
    private static final Logger logger = LoggerFactory.getLogger(ResultSetExtractionLogger.class);
    public static final int MIN_ROWS_TO_LOG = 100000;
    public static final int EACH_ROWS_TO_LOG_AFTER = 20000;

    public static void log(final String codeId, final long startTime, final long rowCount) {
        if (rowCount < MIN_ROWS_TO_LOG) {
            return;
        }
        if (rowCount == MIN_ROWS_TO_LOG) {
            if (logger.isWarnEnabled()) {
                logger.warn(codeId + " retrieved " + rowCount + " Result Set rows in " + (System.currentTimeMillis() - startTime) + " ms at", new Throwable());
            }
        } else if (rowCount % EACH_ROWS_TO_LOG_AFTER == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn(codeId + " retrieved " + rowCount + " Result Set rows in " + (System.currentTimeMillis() - startTime) + " ms");
            }
        }
    }

    public static void logFinish(String codeId, long startTime, long rowCount) {
        if (rowCount < MIN_ROWS_TO_LOG) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(codeId + " DONE. " + rowCount + "  Result Set rows in " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }
}
