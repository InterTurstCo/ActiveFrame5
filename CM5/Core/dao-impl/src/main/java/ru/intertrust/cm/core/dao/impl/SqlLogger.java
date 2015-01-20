package ru.intertrust.cm.core.dao.impl;


import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
import ru.intertrust.cm.core.dao.api.UserTransactionService;

/**
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
@Aspect
public class SqlLogger {
    private static final String DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";

    private static final String PARAMETER_PATTERN = ":\\w";

    private static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    @org.springframework.beans.factory.annotation.Value("${sql.trace.warn.minTime:100}")
    private Long minWarnTime;
    @org.springframework.beans.factory.annotation.Value("${sql.trace.warn.minRows:1}")
    private Long minRowsNum;
    @org.springframework.beans.factory.annotation.Value("${sql.trace.resolveParams:false}")
    private Boolean resolveParams;

    @Autowired
    UserTransactionService userTransactionService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Around("(this(org.springframework.jdbc.core.JdbcOperations) || " +
                "this(org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations)) && " +
                "execution(* *(String, ..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!logger.isTraceEnabled() && !logger.isWarnEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object returnValue = joinPoint.proceed();
        long timing = System.currentTimeMillis() - startTime;

        String query = getSqlQuery(joinPoint);

        int rows = countSqlRows(returnValue, query);

        boolean logWarn = timing >= minWarnTime || rows >= minRowsNum;
        if (logWarn && logger.isWarnEnabled()) {
            query = resolveParameters(query, joinPoint);
            logger.warn(formatLogEntry(query, timing, rows));
        } else if (logger.isTraceEnabled()){
            query = resolveParameters(query, joinPoint);
            logger.trace(formatLogEntry(query, timing, rows));
        }

        return returnValue;
    }

    private String getSqlQuery(ProceedingJoinPoint joinPoint) {
        return joinPoint.getArgs()[0].toString();
    }

    private int countSqlRows(Object returnValue, String query) {
        int rows = -1;

        if (returnValue == null) {
            // не обрабатывается
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
        long timing = System.currentTimeMillis() - startTime;

        String query = getSqlQuery(joinPoint);

        int rows = countSqlRows(returnValue, query);

        query = resolveParameters(query, joinPoint);
        String logEntry = formatLogEntry(query, timing, rows);


        LogTransactionListener listener = null;
        listener = userTransactionService.getListener(LogTransactionListener.class);
        if (listener == null) {
            listener = new LogTransactionListener(startTime, transactionId, transactionTraceConf.getMinTime());
            userTransactionService.addListener(listener);
        }

        listener.addSqlLogEntry(logEntry);

        return returnValue;
    }



    private String resolveParameters(String query, ProceedingJoinPoint joinPoint) {
        if (resolveParams) {
            if (joinPoint.getThis() instanceof JdbcOperations) {
                Object[] parameters = getParametersArray(joinPoint.getArgs());
                query = (parameters == null ? query : fillParameters(query, parameters));
            } else if (joinPoint.getThis() instanceof NamedParameterJdbcOperations) {
                Map<String, Object> parameters = getParametersMap(joinPoint.getArgs());
                query = (parameters == null ? query : fillParameters(query, parameters));
            } else {
                throw new IllegalStateException("SqlLogger intercepts unsupported class type");
            }
        }

        return query;
    }

    private String formatLogEntry(String query, long timing, int rows) {
        StringBuilder traceStringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(traceStringBuilder);
        String format = "SQL Trace: %1$6s %2$7s: %3$s";
        formatter.format(format, timing, "[" + rows + "]", query);

        return traceStringBuilder.toString();
    }

    private Map<String, Object> getParametersMap(Object[] methodArgs) {
        Map<String, Object> parameters = null;
        for (int i = 1; i < methodArgs.length; i++) {
            Object argument = methodArgs[i];
            if (argument instanceof Map) {
                parameters = (Map<String, Object>) argument;
                break;
            }
            if (argument instanceof Map[]) {
                Map[] args = (Map[]) argument;
                if (args.length > 0) {
                    // todo only for one string
                    parameters = (Map<String, Object>) args[0];
                }
                break;
            }
        }
        return parameters;
    }

    private Object[] getParametersArray(Object[] methodArgs) {
        for (int i = 1, n = methodArgs.length; i < n; i++) {
            Object argument = methodArgs[i];
            if (argument instanceof Object[]) {
                return (Object[]) argument;
            }
        }
        return null;
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
        Pattern pattern = Pattern.compile(PARAMETER_PATTERN);
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

    private Map<Integer, String> getParametersPositionMap(String query, Map<String, Object> parameters) {
        int prevIndex;
        int index;

        Map<Integer, String> parametersPositionMap = new HashMap<>();
        for (String parameterName : parameters.keySet()) {
            String parameterWord = ":" + parameterName;
            prevIndex = 0;

            while((index = query.indexOf(parameterWord, prevIndex)) >= 0) {
                parametersPositionMap.put(index, parameterName);
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
