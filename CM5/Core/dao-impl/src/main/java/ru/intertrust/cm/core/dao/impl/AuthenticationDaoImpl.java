package ru.intertrust.cm.core.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.dao.api.AuthenticationDao;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация DAO для работы с системным объектом AuthenticationInfo.
 * @author atsvetkov
 *
 */
public class AuthenticationDaoImpl implements AuthenticationDao {

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations masterJdbcTemplate; // Use for data modifying operations

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations switchableJdbcTemplate; // User for read operations

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


        return masterJdbcTemplate.update(query, paramMap);
    }



    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.AuthenticationDao#existsAuthenticationInfo(java.lang.String)
     */
    @Override
    public boolean existsAuthenticationInfo(String userUid) {
        String query = "select count(*) from " + wrap("authentication_info") + " ai where ai." + wrap("user_uid") + "=:user_uid";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_uid", userUid);
        @SuppressWarnings("deprecation")
        int total = switchableJdbcTemplate.queryForInt(query, paramMap);
        return total > 0;
    }

}
