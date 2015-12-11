package ru.intertrust.cm.core.dao.impl;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.TransactionTrace;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.SqlLoggerEnforcer;
import ru.intertrust.cm.core.dao.api.UserTransactionService;

/**
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
@Aspect
public class SqlLogger {
    private static final String DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";

    private static final String NAMED_PARAMETER_PATTERN = ":\\w";
    private static final String PARAMETER_PATTERN = "\\?";

    public static final ThreadLocal<Long> SQL_PREPARATION_TIME_CACHE = new ThreadLocal<>();
    
    private static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    @org.springframework.beans.factory.annotation.Value("${sql.trace.warn.minTime:100}")
    private Long minWarnTime;
    @org.springframework.beans.factory.annotation.Value("${sql.trace.warn.minRows:1}")
    private Long minRowsNum;
    @org.springframework.beans.factory.annotation.Value("${sql.trace.resolveParams:false}")
    private Boolean resolveParams;
    @org.springframework.beans.factory.annotation.Value("${(sql.trace.output.for.e-tables:true}")
    private Boolean excelTableFormat = true;
    

    @Autowired
    private UserTransactionService userTransactionService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private SqlLoggerEnforcer sqlLoggerEnforcer;

    @Around("(this(org.springframework.jdbc.core.JdbcOperations) || " +
                "this(org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations)) && " +
                "execution(* *(String, ..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!logger.isTraceEnabled() && !logger.isWarnEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object returnValue = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        Long preparationTime = readAndResetPreparationTime();
        if (preparationTime == null) {
            preparationTime = new Long(0);
        }
        Long preparationTimeMillis = getTimeInMilliseconds(preparationTime);
        
        String query = getSqlQuery(joinPoint);

        int rows = countSqlRows(returnValue, query);
        
        boolean logWarn = executionTime >= minWarnTime || rows >= minRowsNum;
        if (sqlLoggerEnforcer.isSqlLoggingEnforced()) {
            query = resolveParameters(query, joinPoint, true);
            logger.info(formatLogEntry(query, preparationTimeMillis, executionTime, rows));
        } else if (logWarn && logger.isWarnEnabled()) {
            query = resolveParameters(query, joinPoint);
            logger.warn(formatLogEntry(query, preparationTimeMillis, executionTime, rows));
        } else if (logger.isTraceEnabled()){
            query = resolveParameters(query, joinPoint);
            logger.trace(formatLogEntry(query, preparationTimeMillis, executionTime, rows));
        }

        return returnValue;
    }

    private String getSqlQuery(ProceedingJoinPoint joinPoint) {
        return joinPoint.getArgs()[0].toString();
    }

    private int countSqlRows(Object returnValue, String query) {
        int rows = -1;

        if (returnValue == null) {
            rows = 0;
        } else if (returnValue instanceof IdentifiableObjectCollection) {
            //  SELECT в случае коллекций получаем количество строк
            IdentifiableObjectCollection result = (IdentifiableObjectCollection) returnValue;
            rows = result.size();
        } else if (returnValue instanceof List) {
            rows = ((List)returnValue).size();
        } else if (returnValue instanceof Integer && query != null && !query.trim().toUpperCase().startsWith("SELECT")) {
            // для INSERT, DELETE, UPDATE
            rows = (Integer)returnValue;
        } else if (returnValue instanceof int[] && query != null && !query.trim().toUpperCase().startsWith("SELECT")) {
            // для batchUpdate (INSERT, DELETE, UPDATE)
            int[] counts = (int[]) returnValue;
            rows = 0;
            for (int count : counts) {
                rows += count;
            }
        } else if (returnValue instanceof int[][] && query != null && !query.trim().toUpperCase().startsWith("SELECT")) {
            // для batchUpdate (INSERT, DELETE, UPDATE)
            int[][] counts = (int[][]) returnValue;
            rows = 0;
            for (int j = 0; j < counts.length; j ++) {
                for (int i = 0; i < counts[j].length; i++) {
                    rows += counts[j][i];
                }
            }
        }
        else {
            // для прочих
            rows = 1;
        }
        return rows;
    }


    @Around("(this(org.springframework.jdbc.core.JdbcOperations) || " +
            "this(org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations)) && " +
            "execution(* *(String, ..))")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {

        TransactionTrace transactionTraceConf = configurationExplorer.getGlobalSettings().getTransactionTrace();

        if (transactionTraceConf == null || !transactionTraceConf.isEnable()) {
            return joinPoint.proceed();
        }

        String transactionId = userTransactionService.getTransactionId();
        if (transactionId == null) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object returnValue = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        Long preparationTime = readAndResetPreparationTime();
        if (preparationTime == null) {
            preparationTime = new Long(0);
        }
        Long preparationTimeMillis = getTimeInMilliseconds(preparationTime);
        String query = getSqlQuery(joinPoint);

        int rows = countSqlRows(returnValue, query);

        query = resolveParameters(query, joinPoint);
        String logEntry = formatLogEntry(query, preparationTimeMillis, executionTime, rows);

        LogTransactionListener listener = null;
        listener = userTransactionService.getListener(LogTransactionListener.class);
        if (listener == null) {
            listener = new LogTransactionListener(startTime, transactionId, transactionTraceConf.getMinTime());
            userTransactionService.addListener(listener);
        }

        listener.addSqlLogEntry(logEntry);
        listener.addPreparationTime(preparationTime);
        return returnValue;
    }

    private long getTimeInMilliseconds(Long nanoTime) {
        return Math.round(nanoTime/1000000d);
    }

    private Long readAndResetPreparationTime() {
        Long preparationTime = SQL_PREPARATION_TIME_CACHE.get();
        SQL_PREPARATION_TIME_CACHE.set(null);
        return preparationTime;
    }


    private String resolveParameters(String query, ProceedingJoinPoint joinPoint) {
        return resolveParameters(query, joinPoint, resolveParams);
    }

    private String resolveParameters(String query, ProceedingJoinPoint joinPoint, boolean isResolve) {
        if (isResolve) {
            if (joinPoint.getThis() instanceof JdbcOperations || joinPoint.getThis() instanceof NamedParameterJdbcOperations) {
                Object[] args = joinPoint.getArgs();
                Object parameters = getParameters(args);

                if (parameters instanceof Object[]) {
                    query = fillParameters(query, (Object[]) parameters);
                } else if (parameters instanceof Map) {
                    if (args.length == 4 && args[3] instanceof BatchPreparedStatementSetter) {
                        query = fillParameters(query, (Map) parameters, (BatchPreparedStatementSetter) args[3]);
                    } else {
                        query = fillParameters(query, (Map) parameters);
                    }
                }
            } else {
                throw new IllegalStateException("SqlLogger intercepts unsupported class type");
            }
        }

        return query;
    }

    public String formatLogEntry(String query, Long preparationTime, long executionTime, int rows) {
        Long totalTime = preparationTime != null ? preparationTime + executionTime : executionTime;

        StringBuilder traceStringBuilder = new StringBuilder();

        Formatter formatter = new Formatter(traceStringBuilder);
        String format = null;
        if (excelTableFormat) {
            format = "SQL Trace:\t%1$s\t%2$s\t%3$s\t%4$s\t%5$s";
        } else {
            format = "SQL Trace: %1$6s (%2$s+%3$s)  [%4$7s]: %5$s";
        }
        formatter.format(format, totalTime, preparationTime, executionTime, rows, query);
        return traceStringBuilder.toString();
    }
    
    private Object getParameters(Object[] methodArgs) {
        Object parameters = null;
        for (int i = 1; i < methodArgs.length; i++) {
            Object argument = methodArgs[i];
            if (argument instanceof Map) {
                parameters = argument;
                break;
            } else if (argument instanceof Collection && !((Collection) argument).isEmpty()) {
                // todo only for one string
                Object firstElement = ((Collection) argument).iterator().next();
                if (firstElement instanceof Map) {
                    parameters = firstElement;
                }
                break;
            } else if (argument instanceof Map[]) {
                Map[] args = (Map[]) argument;
                if (args.length > 0) {
                    // todo only for one string
                    parameters = args[0];
                }
                break;
            } else if (argument instanceof Object[]) {
                parameters = argument;
                break;
            }
        }
        return parameters;
    }

    private String fillParameters(String query, Object[] sqlArgs) {
        StringBuilder queryWithParameters = new StringBuilder();
        int index;
        int prevIndex = 0;

        for (Object argument : sqlArgs) {
            index = query.indexOf("?", prevIndex);
            if (index == -1) {
                break;
            }

            queryWithParameters.append(query.substring(prevIndex, index));
            appendFormattedValue(argument, queryWithParameters);

            prevIndex = index + 1;
        }

        if (prevIndex != query.length()) {
            queryWithParameters.append(query.substring(prevIndex));
        }

        return queryWithParameters.toString();
    }

    protected String fillParameters(String query, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return query;
        }

        Map<Integer, String> parametersPositionMap = getParametersPositionMap(query, parameters);
        if (parametersPositionMap == null || parametersPositionMap.isEmpty()) {
            return query;
        }

        StringBuilder queryWithParameters = new StringBuilder();

        int index;
        int prevIndex = 0;
        Pattern pattern = Pattern.compile(NAMED_PARAMETER_PATTERN);
        Matcher matcher = pattern.matcher(query);

        while (matcher.find()) {

            index = matcher.start();

            String parameterName = parametersPositionMap.get(index);
            if (parameterName == null) {
                continue;
            }

            queryWithParameters.append(query.substring(prevIndex, index));

            Object value = parameters.get(parameterName);
            appendFormattedValue(value, queryWithParameters);

            prevIndex = index + parameterName.length() + 1;

        }

        if (prevIndex != query.length()) {
            queryWithParameters.append(query.substring(prevIndex));
        }

        return queryWithParameters.toString();
    }

    protected String fillParameters(String query, Map<String, Object> parameters, BatchPreparedStatementSetter batchPreparedStatementSetter) {
        if (parameters == null || parameters.isEmpty()) {
            return query;
        }

        StringBuilder queryWithParameters = new StringBuilder();

        int index;
        int prevIndex = 0;
        Pattern pattern = Pattern.compile(PARAMETER_PATTERN);
        Matcher matcher = pattern.matcher(query);

        int parameterIndex = 1;
        while (matcher.find()) {

            index = matcher.start();

            String parameterName = batchPreparedStatementSetter.getParameterName(parameterIndex);
            if (parameterName == null) {
                continue;
            }

            queryWithParameters.append(query.substring(prevIndex, index));

            Object value = parameters.get(parameterName);
            appendFormattedValue(value, queryWithParameters);

            prevIndex = index + 1;
            parameterIndex++;
        }

        if (prevIndex != query.length()) {
            queryWithParameters.append(query.substring(prevIndex));
        }

        return queryWithParameters.toString();
    }

    private Map<Integer, String> getParametersPositionMap(String query, Map<String, Object> parameters) {
        int prevIndex;
        int index;

        Map<Integer, String> parametersPositionMap = new HashMap<>();
        for (String parameterName : parameters.keySet()) {
            String parameterWord = ":" + parameterName;
            prevIndex = 0;

            while ((index = query.indexOf(parameterWord, prevIndex)) >= 0) {
                if (query.indexOf(parameterWord + DomainObjectDao.REFERENCE_TYPE_POSTFIX, prevIndex) != index) {
                    parametersPositionMap.put(index, parameterName);
                }
                prevIndex = index + 1;
            }
        }
        return parametersPositionMap;
    }

    private void appendFormattedValue(Object value, StringBuilder queryWithParameters) {
        if (value == null) {
            queryWithParameters.append("NULL");
        } else if (value instanceof String) {
            queryWithParameters.append("'").append(value.toString().replace("'", "''")).append("'");
        } else if (value instanceof Calendar) {
            Calendar calendarValue = (Calendar) value;
            TimeZone timeZone = TimeZone.getTimeZone("GMT");
            queryWithParameters.append("'").append(ThreadSafeDateFormat.format(calendarValue.getTime(), DATE_PATTERN, timeZone)).append("'");
        } else {
            queryWithParameters.append(value.toString());
        }
    }    
}
