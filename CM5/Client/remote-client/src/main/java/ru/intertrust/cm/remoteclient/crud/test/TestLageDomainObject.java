package ru.intertrust.cm.remoteclient.crud.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Тестирование времени выполнения JDBC запроса при вставке или обновления
 * данных в большую (по ширине) таблицу при разных количествах полей
 * @author larin
 * 
 */
public class TestLageDomainObject {
    public static final int MAX_FIELDS_COUNT = 200;
    public static final int FIELDS_STEP = 10;
    public static final int QUERY_FIELDS_STEP = 10;
    public static final int ITERATIONS_COUNT = 1;

    private ApplicationContext applicationContext;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private boolean useSpring;

    private List<Long> ids = new ArrayList<Long>();
    private Random rnd = new Random();

    public static void main(String[] args) {
        try {
            TestLageDomainObject test = new TestLageDomainObject();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void execute(String[] args) throws SQLException {
        //Создание spring контекста
        applicationContext = new ClassPathXmlApplicationContext("classpath:/TestLageDomainObject.xml");
        jdbcTemplate = applicationContext.getBean(NamedParameterJdbcTemplate.class);
        dataSource = applicationContext.getBean(DataSource.class);

        dropTable();

        // Создание таблицы c разным кол во атрибутов
        List<List<Long>> resultsInsertSpring0 = new ArrayList<List<Long>>();
        List<List<Long>> resultsUpdateSpring0 = new ArrayList<List<Long>>();
        List<List<Long>> resultsInsertSpring1 = new ArrayList<List<Long>>();
        List<List<Long>> resultsUpdateSpring1 = new ArrayList<List<Long>>();

        List<List<Long>> resultsInsertJdbc0 = new ArrayList<List<Long>>();
        List<List<Long>> resultsUpdateJdbc0 = new ArrayList<List<Long>>();

        for (int i = 10; i <= MAX_FIELDS_COUNT; i = i + FIELDS_STEP) {
            System.out.println("Table with " + i + " fields");
            createTable(i);

            useSpring = true;
            resultsInsertSpring0.add(testOperation(i, true, true));
            resultsUpdateSpring0.add(testOperation(i, false, true));
            resultsInsertSpring1.add(testOperation(i, true, false));
            resultsUpdateSpring1.add(testOperation(i, false, false));
            useSpring = false;
            resultsInsertJdbc0.add(testOperation(i, true, true));
            resultsUpdateJdbc0.add(testOperation(i, false, true));

            dropTable();
        }

        printResult("Test Spring Insert Using Params", resultsInsertSpring0);
        printResult("Test Spring Update Using Params", resultsUpdateSpring0);
        printResult("Test Spring Update Not Using Params", resultsInsertSpring1);
        printResult("Test Spring Update Not Using Params", resultsUpdateSpring1);

        printResult("Test JDBC Insert Using Params", resultsInsertJdbc0);
        printResult("Test JDBC Update Using Params", resultsUpdateJdbc0);
    }

    private void printResult(String header, List<List<Long>> results) {
        System.out.println(header);
        System.out.print("U\\F");
        for (int i = 0; i <= MAX_FIELDS_COUNT; i = i + QUERY_FIELDS_STEP) {
            System.out.print("\t" + i);
        }
        System.out.println();

        int fieldInTable = 10;
        for (List<Long> columns : results) {
            System.out.print(fieldInTable);
            for (Long rows : columns) {
                System.out.print("\t" + rows);
            }
            System.out.println();
            fieldInTable = fieldInTable + FIELDS_STEP;
        }
    }

    private void dropTable() {
        try {
            String query = "drop table test_lage_table";
            jdbcTemplate.execute(query, new NullPrepareStatment());
            ids.clear();
        } catch (Exception ignoreEx) {
        }
    }

    private List<Long> testOperation(int fieldsCount, boolean create, boolean usingParams) throws SQLException {
        List<Long> result = new ArrayList<Long>();
        //в цикле выполняем insert c разным количеством полей с шагом 10
        for (int i = 0; i <= fieldsCount; i = i + QUERY_FIELDS_STEP) {
            long time = 0;
            for (int j = 0; j < ITERATIONS_COUNT; j++) {
                if (create) {
                    time += insert(i, usingParams);
                } else {
                    time += update(i, usingParams);
                }
            }
            long workTime = time / ITERATIONS_COUNT;
            result.add(workTime);
            System.out.println((create ? "Create " : "Update ") + (i) + "=" + workTime + " ms");
        }
        return result;
    }

    private long update(int countFields, boolean usingParams) throws SQLException {
        String query = "update test_lage_table set ";
        for (int i = 0; i < countFields; i++) {
            if (i > 0) {
                query += ",";
            }
            if (usingParams) {
                if (useSpring) {
                    query += "field" + i + "= :field" + i;
                } else {
                    query += "field" + i + "= ?";
                }
            } else {
                query += "field" + i + "= null";
            }
        }
        if (useSpring){
            query += " where id = :id";
        }else{
            query += " where id = ?";
        }

        Map<String, Object>[] parameters = new Map[1];
        parameters[0] = new HashMap<String, Object>();

        if (usingParams) {
            for (int i = 0; i < countFields; i++) {
                parameters[0].put("field" + i, null);
            }
        }

        parameters[0].put("id", getRndId());
        PreparedStatement ps = null;
        if (!useSpring && countFields > 0){
            ps = dataSource.getConnection().prepareStatement(query);
            for (int i = 1; i < countFields + 1; i++) {
                ps.setNull(i, Types.VARCHAR);
            }
            ps.setLong(countFields + 1, getNextId());
        }

        long start = System.currentTimeMillis();
        if (countFields > 0) {
            if (useSpring) {
                jdbcTemplate.batchUpdate(query, parameters);
            } else {
                ps.execute();
            }
        }
        return System.currentTimeMillis() - start;
    }

    private Object getRndId() {
        return ids.get(rnd.nextInt(ids.size()));
    }

    private long insert(int countFields, boolean usingParams) throws SQLException {
        String query = "insert into test_lage_table (id";
        for (int i = 0; i < countFields; i++) {
            query += ", field" + i;
        }
        query += ") values (";
        if (useSpring) {
            query += ":id";
        } else {
            query += "?";
        }
        for (int i = 0; i < countFields; i++) {
            if (usingParams) {
                if (useSpring) {
                    query += ", :field" + i;
                } else {
                    query += ", ?";
                }
            } else {
                query += ", null";
            }
        }
        query += ")";

        Map<String, Object>[] parameters = new Map[1];
        parameters[0] = new HashMap<String, Object>();
        parameters[0].put("id", getNextId());

        if (usingParams) {
            for (int i = 0; i < countFields; i++) {
                parameters[0].put("field" + i, null);
            }
        }

        PreparedStatement ps = null;
        if (!useSpring) {
            ps = dataSource.getConnection().prepareStatement(query);
            ps.setLong(1, getNextId());
            for (int i = 2; i < countFields + 2; i++) {
                ps.setNull(i, Types.VARCHAR);
            }
        }

        long start = System.currentTimeMillis();
        if (useSpring) {
            jdbcTemplate.batchUpdate(query, parameters);
        } else {
            ps.execute();
        }
        return System.currentTimeMillis() - start;
    }

    private void createTable(int fieldsCount) {
        String query = "create table test_lage_table (";
        query += "id bigint NOT NULL,";
        for (int i = 0; i < fieldsCount; i++) {
            query += "field" + i + " character varying(1024), ";
        }
        query += "CONSTRAINT pk_test_lage_table PRIMARY KEY (id)";
        query += ")";
        jdbcTemplate.execute(query, new NullPrepareStatment());
    }

    private void createSeq() {
        String query = "CREATE SEQUENCE test_lage_table_sq";
        jdbcTemplate.execute(query, new NullPrepareStatment());
    }

    private boolean checkTable() {
        String query = "select count(id) from test_lage_table";
        try {
            jdbcTemplate.queryForList(query, (Map) null);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private long getNextId() {
        String query = "select nextval('test_lage_table_sq')";
        long result = jdbcTemplate.queryForLong(query, (Map) null);
        ids.add(result);
        return result;
    }

    class NullPrepareStatment implements PreparedStatementCallback<Boolean> {

        @Override
        public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
            return ps.execute();
        }
    }

    private String getRndString() {
        return "_" + System.nanoTime();
    }
}
