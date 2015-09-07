package ru.intertrust.cm.test.extension;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter="test_type_14")
public class AfterSaveTestType14 implements AfterSaveExtensionHandler{

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;
    
    
    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        //Поиск персоны
        IdentifiableObjectCollection collection =  collectionsService.findCollectionByQuery("select id from test_type_13");
        
        //Получаем с блокировкой, Меняем поле и сохраняем
        DomainObject testType13 = crudService.findAndLock(collection.get(0).getId());
        String lastName = testType13.getString("description");
        
        Pattern p = Pattern.compile("[^\\d]*(\\d+)");
        Matcher m = p.matcher(lastName);
        long num = 0;
        if (m.find()) {
            num = Long.parseLong(m.group(1));
            num++;
        }
        
        testType13.setString("description", "description-" + num);
        testType13.setString("description2", "description-" + num);
        System.out.println("person1 last name = " + testType13.getString("description"));
        crudService.save(testType13);        
    }

}
