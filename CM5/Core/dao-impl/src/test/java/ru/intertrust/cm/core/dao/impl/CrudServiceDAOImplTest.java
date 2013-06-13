package ru.intertrust.cm.core.dao.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.GenericBusinessObject;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;

/**
 * Юнит тест для CrudServiceDAOImpl
 *
 * @author skashanski
 *
 */
public class CrudServiceDAOImplTest {

    private BusinessObjectConfig businessObjectConfig;

    private final CrudServiceDAOImpl crudServiceDAOImpl = new CrudServiceDAOImpl();

    @Before
    public void setUp() throws Exception {
        initBusinessObjectConfig();
    }

    @Test
    public void testGenerateCreateQuery() throws Exception {

        BusinessObject businessObject = new GenericBusinessObject();
        businessObject.setTypeName(businessObjectConfig.getName());
        businessObject.setValue("EMail", new StringValue("testCreate@test.com"));
        businessObject.setValue("Login", new StringValue("userCreate"));
        businessObject.setValue("Password", new StringValue("passCreate"));

        Date currentDate = new Date();
        businessObject.setCreatedDate(currentDate);
        businessObject.setModifiedDate(currentDate);

        String checkCreateQuery =
                "insert into PERSON (ID , CREATED_DATE, UPDATED_DATE, EMAIL,LOGIN,PASSWORD) values (:id , :created_date, :updated_date, :email,:login,:password)";

        String query = crudServiceDAOImpl.generateCreateQuery(businessObject, businessObjectConfig);
        assertEquals(checkCreateQuery, query);

    }

    @Test
    public void testGenerateUpdateQuery() throws Exception {

        BusinessObject businessObject = new GenericBusinessObject();
        businessObject.setTypeName(businessObjectConfig.getName());
        businessObject.setValue("EMail", new StringValue("testUpdate@test.com"));
        businessObject.setValue("Login", new StringValue("userUpdate"));
        businessObject.setValue("Password", new StringValue("passUpdate"));

        Date currentDate = new Date();
        businessObject.setId(new RdbmsId("person", 1));
        businessObject.setCreatedDate(currentDate);
        businessObject.setModifiedDate(currentDate);

        String checkUpdateQuery = "update PERSON set UPDATED_DATE=:current_date, EMAIL=:email,LOGIN=:login,PASSWORD=:password where ID=:id and UPDATED_DATE=:updated_date";

        String query = crudServiceDAOImpl.generateUpdateQuery(businessObject, businessObjectConfig);
        assertEquals(checkUpdateQuery, query);

    }

    @Test
    public void testGenerateDeleteQuery() throws Exception {

        String checkDeleteQuery = "delete from PERSON where id=:id";

        String query = crudServiceDAOImpl.generateDeleteQuery(businessObjectConfig);
        assertEquals(checkDeleteQuery, query);

    }

    @Test
    public void testGenerateExistsQuery() throws Exception {

        String checkExistsQuery = "select id from PERSON where id=:id";

        String query = crudServiceDAOImpl.generateExistsQuery(businessObjectConfig);
        assertEquals(checkExistsQuery, query);

    }

    private void initBusinessObjectConfig() {

        /*
         * Создаем конфигурацию следующего ввида <businessObject name="Person">
         * <fields> <string name="EMail" length="128" /> <string name="Login"
         * length="64" not-null="true" /> <password name="Password" length="128"
         * /> </fields> <uniqueKey> <!-- This key means automatic key + index
         * creation--> <field name="EMail"/> </uniqueKey> </businessObject>
         */

        businessObjectConfig = new BusinessObjectConfig();
        businessObjectConfig.setName("person");
        StringFieldConfig email = new StringFieldConfig();
        email.setName("EMail");
        email.setLength(128);
        businessObjectConfig.getFieldConfigs().add(email);

        StringFieldConfig Login = new StringFieldConfig();
        Login.setName("Login");
        Login.setLength(64);
        Login.setNotNull(true);
        businessObjectConfig.getFieldConfigs().add(Login);

        StringFieldConfig Password = new StringFieldConfig();
        Password.setName("Password");
        Password.setLength(128);
        businessObjectConfig.getFieldConfigs().add(Password);

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        businessObjectConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        UniqueKeyFieldConfig uniqueKeyFieldConfig1 = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig1.setName("EMail");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig1);

    }

}
