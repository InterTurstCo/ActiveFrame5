package ru.intertrust.cm.core.service.it.extension;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.auth.login.LoginContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.service.it.IntegrationTestBase;

@RunWith(Arquillian.class)
public class TransactionalExtensionsIT extends IntegrationTestBase{
    @Inject
    private UserTransaction utx;

    @EJB
    private CrudService.Remote crudService;
    
    @Test
    public void testAfterSaveAfterTransactionCommit() throws Exception{
        LoginContext lc = login("admin", "admin");
        lc.login();
        
        utx.begin();

        TestAftersaveAfterCommitExtPointhandler.clear();
        //Создаем
        String oldName = "Name-" + System.nanoTime();
        String oldDescription = "Description-" + System.nanoTime();
        DomainObject organization = crudService
                .createDomainObject("Organization");
        organization.setString("Name", oldName);
        organization.setString("Description", oldDescription);
        //Сохраняем первый раз
        organization = crudService.save(organization);        

        //Меняем
        organization.setString("Name", "Name-" + System.nanoTime());
        organization.setString("Description", "Description-" + System.nanoTime());
        //Сохраняем второй раз
        organization = crudService.save(organization);
        
        DomainObject organizationForDelete = crudService
                .createDomainObject("Organization");
        organizationForDelete.setString("Name", "Name-" + System.nanoTime());
        organizationForDelete.setString("Description", "Description-" + System.nanoTime());
        //Сохраняем первый раз
        organizationForDelete = crudService.save(organizationForDelete);        
        
        crudService.delete(organizationForDelete.getId());
        
        utx.commit();
        
        lc.logout();
        
        assertTrue(TestAftersaveAfterCommitExtPointhandler.save.size() == 2);
        assertTrue(TestAftersaveAfterCommitExtPointhandler.save.get(0).modifications.size() == 4);
        FieldModification fieldName = getFieldModification("Name", TestAftersaveAfterCommitExtPointhandler.save.get(0).modifications);
        FieldModification fieldDescription = getFieldModification("Description", TestAftersaveAfterCommitExtPointhandler.save.get(0).modifications);
        //Проверяем мерж измененных полей
        assertTrue(fieldName.getBaseValue()==null && fieldName.getComparedValue().get().equals(organization.getString("Name")));
        assertTrue(fieldDescription.getBaseValue()==null && fieldDescription.getComparedValue().get().equals(organization.getString("Description")));
        //Проверяем количество созданных
        assertTrue(TestAftersaveAfterCommitExtPointhandler.create.size() == 2);
        //Проверка количества удаленных
        assertTrue(TestAftersaveAfterCommitExtPointhandler.delete.size() == 1);
        
    }    
    
    private FieldModification getFieldModification(String name, List<FieldModification> fields){
        FieldModification result = null;
        for (FieldModification field : fields) {
            if (field.getName().equalsIgnoreCase(name)){
                result = field;
                break;
            }
        }
        return result;
    }
}
