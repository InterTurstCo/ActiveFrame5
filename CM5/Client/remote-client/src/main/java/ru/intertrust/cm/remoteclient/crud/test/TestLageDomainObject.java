package ru.intertrust.cm.remoteclient.crud.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.naming.OperationNotSupportedException;
import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.dao.impl.BatchPreparedStatementSetter;
import ru.intertrust.cm.core.dao.impl.Query;

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
    public static final int ITERATIONS_COUNT = 5;

    private ApplicationContext applicationContext;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private JdbcOperations jdbcOperations;
    private DataSource dataSource;

    public enum TestType{
        NamedParameterJdbctemplate,
        PrepareStatment,
        JdbcOperation
    }

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

    private void execute(String[] args) throws SQLException, OperationNotSupportedException {
        //Создание spring контекста
        applicationContext = new ClassPathXmlApplicationContext("classpath:/TestLageDomainObject.xml");
        jdbcTemplate = applicationContext.getBean(NamedParameterJdbcTemplate.class);
        dataSource = applicationContext.getBean(DataSource.class);
        jdbcOperations = applicationContext.getBean(JdbcOperations.class);

        dropTable();

        // Создание таблицы c разным кол во атрибутов
        List<List<Long>> resultsInsertNameParam = new ArrayList<List<Long>>();
        List<List<Long>> resultsUpdateNameParam = new ArrayList<List<Long>>();
        List<List<Long>> resultsInsertJdbcOperation = new ArrayList<List<Long>>();
        List<List<Long>> resultsUpdateJdbcOperation = new ArrayList<List<Long>>();
        List<List<Long>> resultsInsertPrepareStatment = new ArrayList<List<Long>>();
        List<List<Long>> resultsUpdatePrepareStatment = new ArrayList<List<Long>>();

        for (int i = 10; i <= MAX_FIELDS_COUNT; i = i + FIELDS_STEP) {
            System.out.println("Table with " + i + " fields");
            createTable(i);

            resultsInsertNameParam.add(testOperation(i, true, TestType.NamedParameterJdbctemplate));
            resultsUpdateNameParam.add(testOperation(i, false, TestType.NamedParameterJdbctemplate));
            resultsInsertJdbcOperation.add(testOperation(i, true, TestType.JdbcOperation));
            resultsUpdateJdbcOperation.add(testOperation(i, false, TestType.JdbcOperation));
            resultsInsertPrepareStatment.add(testOperation(i, true, TestType.PrepareStatment));
            resultsUpdatePrepareStatment.add(testOperation(i, false, TestType.PrepareStatment));

            dropTable();
        }

        printResult("Test NamedParameterJdbctemplate Insert", resultsInsertNameParam);
        printResult("Test NamedParameterJdbctemplate Update", resultsUpdateNameParam);
        printResult("Test JdbcOperation Insert", resultsInsertJdbcOperation);
        printResult("Test JdbcOperation Update", resultsUpdateJdbcOperation);
        printResult("Test PrepareStatment Insert", resultsInsertPrepareStatment);
        printResult("Test PrepareStatment Update", resultsUpdatePrepareStatment);
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

    private List<Long> testOperation(int fieldsCount, boolean create, TestType testType) throws SQLException, OperationNotSupportedException {
        List<Long> result = new ArrayList<Long>();
        //в цикле выполняем insert c разным количеством полей с шагом QUERY_FIELDS_STEP
        for (int i = 0; i <= fieldsCount; i = i + QUERY_FIELDS_STEP) {
            long time = 0;
            for (int j = 0; j < ITERATIONS_COUNT; j++) {
                if (create) {
                    switch (testType) {
                        case NamedParameterJdbctemplate:
                            time += insertUsingNamedParameterJdbcTemplate(i);
                            break;
                        case PrepareStatment:
                            time += insertUsingPrepareStatment(i);
                            break;
                        case JdbcOperation:
                            time += insertUsingJdbcOperation(i);                            
                            break;
                        default:
                            throw new OperationNotSupportedException();
                    }                    
                } else {
                    switch (testType) {
                        case NamedParameterJdbctemplate:
                            time += updateUsingNamedParameterJdbcTemplate(i);
                            break;
                        case PrepareStatment:
                            time += updateUsingPrepareStatment(i);
                            break;
                        case JdbcOperation:
                            time += updateUsingJdbcOperation(i);                            
                            break;
                        default:
                            throw new OperationNotSupportedException();
                    }                    
                }
            }
            long workTime = time / ITERATIONS_COUNT;
            result.add(workTime);
            System.out.println(testType.toString() + " " + (create ? "Create " : "Update ") + (i) + "=" + workTime + " ms");
        }
        return result;
    }

    private long updateUsingNamedParameterJdbcTemplate(int countFields) throws SQLException {
        String query = "update test_lage_table set ";
        for (int i = 0; i < countFields; i++) {
            if (i > 0) {
                query += ",";
            }
            query += "field" + i + "= :field" + i;
        }
        query += " where id = :id";

        Map<String, Object>[] parameters = new Map[1];
        parameters[0] = new HashMap<String, Object>();

        for (int i = 0; i < countFields; i++) {
            parameters[0].put("field" + i, null);
        }

        parameters[0].put("id", getRndId());

        long start = System.currentTimeMillis();
        if (countFields > 0) {
            jdbcTemplate.batchUpdate(query, parameters);
        }
        return System.currentTimeMillis() - start;
    }

    private long updateUsingPrepareStatment(int countFields) throws SQLException {
        String query = "update test_lage_table set ";
        for (int i = 0; i < countFields; i++) {
            if (i > 0) {
                query += ",";
            }
            query += "field" + i + "= ?";
        }
        query += " where id = ?";

        PreparedStatement ps = null;
        if (countFields > 0){
            ps = dataSource.getConnection().prepareStatement(query);
            for (int i = 1; i < countFields + 1; i++) {
                ps.setNull(i, Types.VARCHAR);
            }
            ps.setLong(countFields + 1, getRndId());
        }

        long start = System.currentTimeMillis();
        if (countFields > 0) {
            ps.execute();
        }
        return System.currentTimeMillis() - start;
    }
    
    private long updateUsingJdbcOperation(int countFields) throws SQLException {
        String queryString = "update test_lage_table set ";
        for (int i = 0; i < countFields; i++) {
            if (i > 0) {
                queryString += ",";
            }
            queryString += "field" + i + "= ?";
        }
        queryString += " where id = ?";

        List<Map<String, Object>> parameters = new ArrayList<>(1);
        
        
        Map<String, Object> parameter = new HashMap<String, Object>();
        Query query = new Query();

        for (int i = 0; i < countFields; i++) {
            parameter.put("field" + i, null);
            List<Integer> index = new ArrayList<Integer>();
            index.add(i+1);
            query.addLongParameter("field" + i);
        }

        parameter.put("id", getRndId());
        parameters.add(parameter);

        query.addLongParameter("id");
        
        BatchPreparedStatementSetter batchPreparedStatementSetter = new BatchPreparedStatementSetter(query);
        
        long start = System.currentTimeMillis();
        if (countFields > 0) {
            jdbcOperations.batchUpdate(queryString, parameters, 1, batchPreparedStatementSetter);
        }
        long result = System.currentTimeMillis() - start; 
        return result;
    }
    
    private Long getRndId() {
        return ids.get(rnd.nextInt(ids.size()));
    }

    private long insertUsingNamedParameterJdbcTemplate(int countFields) throws SQLException {
        String query = "insert into test_lage_table (id";
        for (int i = 0; i < countFields; i++) {
            query += ", field" + i;
        }
        query += ") values (";
        query += ":id";
        for (int i = 0; i < countFields; i++) {
            query += ", :field" + i;
        }
        query += ")";

        Map<String, Object>[] parameters = new Map[1];
        parameters[0] = new HashMap<String, Object>();
        parameters[0].put("id", getNextId());

        for (int i = 0; i < countFields; i++) {
            parameters[0].put("field" + i, null);
        }

        long start = System.currentTimeMillis();
        jdbcTemplate.batchUpdate(query, parameters);
        return System.currentTimeMillis() - start;
    }

    private long insertUsingPrepareStatment(int countFields) throws SQLException {
        String query = "insert into test_lage_table (id";
        for (int i = 0; i < countFields; i++) {
            query += ", field" + i;
        }
        query += ") values (";
        query += "?";
        for (int i = 0; i < countFields; i++) {
            query += ", ?";
        }
        query += ")";

        PreparedStatement ps = null;
        ps = dataSource.getConnection().prepareStatement(query);
        ps.setLong(1, getNextId());
        for (int i = 2; i < countFields + 2; i++) {
            ps.setNull(i, Types.VARCHAR);
        }

        long start = System.currentTimeMillis();
        ps.execute();
        return System.currentTimeMillis() - start;
    }
    
    private long insertUsingJdbcOperation(int countFields) throws SQLException {
        String queryString = "insert into test_lage_table (id";
        for (int i = 0; i < countFields; i++) {
            queryString += ", field" + i;
        }
        queryString += ") values (";
        queryString += "?";
        for (int i = 0; i < countFields; i++) {
            queryString += ", ?";
        }
        queryString += ")";

        List<Map<String, Object>> parameters = new ArrayList<>(1);        
        
        Map<String, Object> parameter = new HashMap<String, Object>();
        Query query = new Query();

        for (int i = 0; i < countFields; i++) {
            parameter.put("field" + i, null);
            List<Integer> index = new ArrayList<Integer>();
            index.add(i+2);
            query.addLongParameter("field" + i);
        }

        parameter.put("id", getNextId());
        parameters.add(parameter);

        query.addLongParameter("id");
        
        BatchPreparedStatementSetter batchPreparedStatementSetter = new BatchPreparedStatementSetter(query);
        
        long start = System.currentTimeMillis();
        jdbcOperations.batchUpdate(queryString, parameters, 1, batchPreparedStatementSetter);
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
        Long result = jdbcTemplate.queryForObject(query, (Map) null, Long.class);
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
