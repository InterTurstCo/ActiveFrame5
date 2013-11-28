package ru.intertrust.cm.core.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.dao.api.AuthenticationDao;

/**
 * Реализация DAO для работы с системным объектом AuthenticationInfo.
 * @author atsvetkov
 *
 */
public class AuthenticationDaoImpl implements AuthenticationDao {

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.AuthenticationDao#insertAuthenticationInfo(ru.intertrust.cm.core.business.api.dto.AuthenticationInfo)
     */
    @Override
    public int insertAuthenticationInfo(AuthenticationInfoAndRole authenticationInfo) {
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



    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.AuthenticationDao#existsAuthenticationInfo(java.lang.String)
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
