package ru.intertrust.cm.test.extension;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter="test_type_14")
public class AfterSaveTestType14 implements AfterSaveExtensionHandler{

    @Autowired
    private PersonManagementService personManagementService;

    @Autowired
    private CrudService crudService;
    
    
    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        //Поиск персоны
        Id person1Id = personManagementService.getPersonId("person1");
        
        //Получаем с блокировкой, Меняем поле и сохраняем
        DomainObject person1 = crudService.findAndLock(person1Id);
        String lastName = person1.getString("LastName");
        
        Pattern p = Pattern.compile("[^\\d]*(\\d+)");
        Matcher m = p.matcher(lastName);
        int num = 0;
        if (m.find()) {
            num = Integer.parseInt(m.group(1));
            num++;
        }
        
        person1.setString("LastName", "LastName-" + num);
        System.out.println("person1 last name = " + person1.getString("LastName"));
        crudService.save(person1);        
    }

}
