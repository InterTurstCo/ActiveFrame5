package ru.intertrust.cm.core.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.dao.api.AuthenticationDAO;

/**
 * Реализация DAO для работы с системным объектом AuthenticationInfo.
 * @author atsvetkov
 *
 */
public class AuthenticationDAOImpl implements AuthenticationDAO {

    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Устанавливает источник соединений
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.AuthenticationDAO#insertAuthenticationInfo(ru.intertrust.cm.core.business.api.dto.AuthenticationInfo)
     */
    @Override
    public int insertAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        String query = "insert into authentication_info (id, user_uid, created_date, updated_date, password) values (:id, :user_uid, :created_date, :updated_date, :password)";

        Date currentDate = new Date();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", authenticationInfo.getId());
        paramMap.put("user_uid", authenticationInfo.getUserUid());
        paramMap.put("password", authenticationInfo.getPassword());
        paramMap.put("created_date", currentDate);
        paramMap.put("updated_date", currentDate);


        return jdbcTemplate.update(query, paramMap);
    }





    @Override
    public int insertRole(BusinessObject role) {

        String query = "insert into employee_role (id, role, created_date, updated_date, employee_id) values (:id, :role, :created_date, :updated_date, :employee_id)";

        Date currentDate = new Date();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", role.getId());
        paramMap.put("created_date", currentDate);
        paramMap.put("updated_date", currentDate);
        paramMap.put("role", role.getValue("Role"));
        paramMap.put("employee_id", role.getValue("Employee Id"));


        return jdbcTemplate.update(query, paramMap);

    }

    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.AuthenticationDAO#existsAuthenticationInfo(java.lang.String)
     */
    @Override
    public boolean existsAuthenticationInfo(String userUid) {
        String query = "select count(*) from authentication_info ai where ai.user_uid=:user_uid";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_uid", userUid);
        @SuppressWarnings("deprecation")
        int total = jdbcTemplate.queryForInt(query, paramMap);
        return total > 0;
    }

}
