package ru.intertrust.cm.core.dao.impl;

import org.mockito.ArgumentMatcher;

public class SqlStatementMatcher extends ArgumentMatcher<String> {

    private String masterSql;
    
    public SqlStatementMatcher(String masterSql) {
        this.masterSql = normalize(masterSql);
    }

    @Override
    public boolean matches(Object argument) {
        if (!String.class.equals(argument.getClass())) {
            return false;
        }
        String sql = (String) argument;
        return masterSql.equalsIgnoreCase(normalize(sql));
    }

    private String normalize(String sql) {
        // Очень простой вариант (почти) эквивалентного приведения SQL к стандартному виду
        // 18.05.2016 - А.П. - После перевода DoelResolver на использование CollectionsDao добавление кавычек
        // в запрос выполняется внутри этого сервиса. Чтобы не переписывать все запросы в тестах,
        // просто вырезаем кавычки из сравниваемых строк.
        return sql.trim().replaceAll("\\s+", " ").replaceAll("\"", "");
    }
}
