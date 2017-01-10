package ru.intertrust.cm.core.dao.impl;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.TransactionTrace;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.SqlLoggerEnforcer;
import ru.intertrust.cm.core.dao.api.UserTransactionService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
@Aspect
public class SqlLogger {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

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
    @org.springframework.beans.factory.annotation.Value("${sql.trace.output.for.e-tables:true}")
    private Boolean excelTableFormat = true;
    @org.springframework.beans.factory.annotation.Value("${sql.trace.output.transactionId:false}")
    private Boolean showTransactionId = false;
    @org.springframework.beans.factory.annotation.Value("${sql.trace.output.datasource:true}")
    private Boolean showDatasource = true;
    

    @Autowired
    private UserTransactionService userTransactionService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private SqlLoggerEnforcer sqlLoggerEnforcer;

    @Autowired
    private JdbcOperations masterJdbcOperations;

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

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
            List<String> resolvedQueries = resolveParameters(query, joinPoint, true);
            formatLogEntries(resolvedQueries, preparationTimeMillis, executionTime, rows, getDatasource(joinPoint));
            for (String logEntry : resolvedQueries) {
                logger.info(logEntry);
            }
        } else if (logWarn && logger.isWarnEnabled()) {
            List<String> resolvedQueries = resolveParameters(query, joinPoint);
            formatLogEntries(resolvedQueries, preparationTimeMillis, executionTime, rows, getDatasource(joinPoint));
            for (String logEntry : resolvedQueries) {
                logger.warn(logEntry);
            }
        } else if (logger.isTraceEnabled()){
            List<String> resolvedQueries = resolveParameters(query, joinPoint);
            formatLogEntries(resolvedQueries, preparationTimeMillis, executionTime, rows, getDatasource(joinPoint));
            for (String logEntry : resolvedQueries) {
                logger.trace(logEntry);
            }
        }

        return returnValue;
    }

    private String getDatasource(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getThis();
        if (target == masterJdbcOperations || target == masterJdbcTemplate) {
            return "MASTER";
        } else {
            return currentDataSourceContext.getDescription();
        }
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

        List<String> resolvedQueries = resolveParameters(query, joinPoint);
        formatLogEntries(resolvedQueries, preparationTimeMillis, executionTime, rows, getDatasource(joinPoint));

        LogTransactionListener listener = null;
        listener = userTransactionService.getListener(LogTransactionListener.class);
        if (listener == null) {
            listener = new LogTransactionListener(startTime, transactionId, transactionTraceConf.getMinTime());
            userTransactionService.addListener(listener);
        }

        for (String logEntry : resolvedQueries) {
            listener.addSqlLogEntry(logEntry);
        }
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


    private List<String> resolveParameters(String query, ProceedingJoinPoint joinPoint) {
        return resolveParameters(query, joinPoint, resolveParams);
    }

    private List<String> resolveParameters(String query, ProceedingJoinPoint joinPoint, boolean isResolve) {
        if (!(joinPoint.getThis() instanceof JdbcOperations) &&
                !(joinPoint.getThis() instanceof NamedParameterJdbcOperations)) {
            throw new IllegalStateException("SqlLogger intercepts unsupported class type");
        }

        if (!isResolve) {
            return singletonList(query);
        }

        List<String> resolvedQueries = null;

        Object[] args = joinPoint.getArgs();

        if (args == null || args.length < 2) {
            return singletonList(query);
        }

        Object parameters = args[1];

        if (parameters instanceof Map[]) {
            resolvedQueries = fillParameters(query, (Map[]) parameters);
        } else if (parameters instanceof Object[]) {
            query = fillParameters(query, (Object[]) parameters);
            resolvedQueries = singletonList(query);
        } else if (parameters instanceof Map) {
            query = fillParameters(query, (Map) parameters);
            resolvedQueries = singletonList(query);
        } else if (parameters instanceof Collection &&
                args.length == 4 && args[3] instanceof BatchPreparedStatementSetter) {
            resolvedQueries = fillParameters(query, (Collection) parameters, (BatchPreparedStatementSetter) args[3]);
        } else {
            resolvedQueries = singletonList(query);
        }

        return resolvedQueries;
    }

    public void formatLogEntries(List<String> queries, Long preparationTime, long executionTime, int rows, String datasource) {
        if (queries == null || queries.isEmpty()) {
            return;
        }

        Long totalTime = preparationTime != null ? preparationTime + executionTime : executionTime;

        final String query = queries.get(0);
        StringBuilder traceStringBuilder = new StringBuilder(70 + query.length()).append("SQL Trace:");
        if (excelTableFormat) {
            if (showDatasource) {
                traceStringBuilder.append("\t").append(datasource);
            }
            if (showTransactionId) {
                traceStringBuilder.append("\t").append(userTransactionService.getTransactionId());
            }
            traceStringBuilder.append("\t").append(totalTime)
                    .append("\t").append(preparationTime)
                    .append("\t").append(executionTime)
                    .append("\t").append(rows);
            if (!query.substring(0, 4).toLowerCase().equals("with")) {
                traceStringBuilder.append("\t\t").append(query);
            } else {
                final int firstRBracket = query.indexOf(")");
                if (firstRBracket < 0 || firstRBracket >= query.length() - 1) {
                    traceStringBuilder.append("\t\t").append(query);
                } else {
                    final int splitIndex = firstRBracket + 1;
                    final String withPart = query.substring(0, splitIndex);
                    final String queryItself = query.charAt(splitIndex) == ' ' ? query.substring(splitIndex + 1) : query.substring(splitIndex);
                    traceStringBuilder.append("\t").append(withPart).append("\t").append(queryItself);
                }
            }
        } else {
            if (showDatasource) {
                traceStringBuilder.append(" ").append(datasource);
            }
            if (showTransactionId) {
                traceStringBuilder.append(" ").append(userTransactionService.getTransactionId());
            }
            traceStringBuilder.append(" ").append(totalTime)
                    .append(" (").append(preparationTime).append('+').append(executionTime).append(")")
                    .append(" [").append(rows).append(']')
                    .append(": ").append(query);
        }
        int totalParamsExceptQuery = 4;
        if (showDatasource) {
            ++totalParamsExceptQuery;
        }
        if (showTransactionId) {
            ++totalParamsExceptQuery;
        }

        queries.set(0, traceStringBuilder.toString());

        if (queries.size() == 1) {
            return;
        }

        String delim = excelTableFormat ? "\t" : " ";
        for (int i = 1; i < queries.size(); i ++) {
            traceStringBuilder.setLength(0);
            traceStringBuilder.append("SQL Trace:");
            for (int j = 0; j < totalParamsExceptQuery; ++j) {
                traceStringBuilder.append(delim);
            }
            traceStringBuilder.append(queries.get(i));
            queries.set(i, traceStringBuilder.toString());
        }
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

    protected List<String> fillParameters(String query, Map<String, Object>[] parametersArray) {
        if (parametersArray == null || parametersArray.length == 0) {
            return singletonList(query);
        }

        Map<Integer, String> parametersPositionMap = getParametersPositionMap(query, parametersArray[0]);
        if (parametersPositionMap == null || parametersPositionMap.isEmpty()) {
            return singletonList(query);
        }

        List<String> resolvedQueries = new ArrayList<>(parametersArray.length);
        for (Map<String, Object> parameters : parametersArray) {
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

            resolvedQueries.add(queryWithParameters.toString());
        }

        return resolvedQueries;
    }

    protected List<String> fillParameters(String query, Collection<Map<String, Object>> parametersCollection,
                                          BatchPreparedStatementSetter batchPreparedStatementSetter) {
        if (parametersCollection == null || parametersCollection.isEmpty()) {
            return singletonList(query);
        }

        List<String> resolvedQueries = new ArrayList<>(parametersCollection.size());
        for(Map<String, Object> parameters : parametersCollection) {
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

            resolvedQueries.add(queryWithParameters.toString());
        }

        return resolvedQueries;
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

    private List<String> singletonList(String element) {
        List<String> result = new ArrayList<>(1);
        result.add(element);
        return result;
    }
}
