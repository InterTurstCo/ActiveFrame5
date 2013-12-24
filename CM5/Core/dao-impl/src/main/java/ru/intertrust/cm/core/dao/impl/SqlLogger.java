package ru.intertrust.cm.core.dao.impl;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.SqlTrace;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
@Aspect
public class SqlLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Around("(this(org.springframework.jdbc.core.JdbcOperations) || " +
                "this(org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations)) && " +
                "execution(* *(String, ..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object returnValue = joinPoint.proceed();
        long timing = System.currentTimeMillis() - startTime;

        String query = joinPoint.getArgs()[0].toString();

        SqlTrace configuration = getConfiguration();
        if (!configuration.isEnable()) {
            return returnValue;
        }

        if (timing < configuration.getMinTime()){
            return returnValue;
        }

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
        } else {
            // для прочих
            rows = 1;
        }

        if (rows < configuration.getMinRows()){
            return returnValue;
        }

        if (configuration.isResolveParameters()) {
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

        StringBuilder traceStringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(traceStringBuilder);
        String format = "SQL Trace: %1$6s %2$7s: %3$s";
        formatter.format(format, timing, "[" + rows + "]", query);

        System.out.println(traceStringBuilder.toString());
        return returnValue;
    }

    private Map<String, Object> getParametersMap(Object[] methodArgs) {
        Map<String, Object> parameters = null;
        for (int i = 1; i < methodArgs.length; i++) {
            Object argument = methodArgs[i];
            if (argument instanceof Map) {
                parameters = (Map<String, Object>) argument;
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

    private String fillParameters(String query, Map<String, Object> parameters) {
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
        while((index = query.indexOf(":", prevIndex)) >= 0) {
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
            DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            queryWithParameters.append("'").append(dateFormatter.format(calendarValue.getTime())).append("'");
        } else {
            queryWithParameters.append(value.toString());
        }
    }

    private SqlTrace getConfiguration() {
        GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();

        if (globalSettings != null) {
            return globalSettings.getSqlTrace();
        } else {
            SqlTrace configuration = new SqlTrace();
            configuration.setEnable(false);
            return configuration;
        }
    }
}
