package ru.intertrust.cm.performance.dataset.generatefields;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.performance.dataset.DatasetGenerationServiceImpl;
import ru.intertrust.cm.performance.dataset.xmltypes.ChildrenType;
import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

/**
 * Класс предназначен для анализа поля Children в xml-инструкции и созданию по
 * нему связанных объектов
 * */
public class GenerateChildrenField {
    protected String field;
    protected Id value;
    protected boolean ignore;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;


    public void generateField(DomainObject domainObject, List<TemplateType> templateList, ObjectType objectType, DatasetGenerationServiceImpl dgsi) throws IOException, NoSuchAlgorithmException {

        domainObjectDao = dgsi.getDomainObjectDao();

        List<DomainObject> domainObjects = new ArrayList<DomainObject>();
        List<DomainObject> savedDomainObjects = new ArrayList<DomainObject>();
        // получаем список полей из инструкции
        List<FieldType> fields = objectType.getStringOrDateTimeOrReference();
        // определяем тип дочернего поля
        ChildrenType childrenType = null;
        // получим объект описание поля дочернего типа
        for (FieldType field : fields) {
            if (field instanceof ChildrenType) {
                childrenType = (ChildrenType) field;

                // получим количество создаваемых связанных объектов
                int count = 0;
                if (childrenType.getQuantity() != null) {
                    // количество из константы
                    count = childrenType.getQuantity();
                } else {
                    // количество из интервала
                    Random rnd = new Random(System.currentTimeMillis());
                    count = childrenType.getMinQuantity() + rnd.nextInt(childrenType.getMaxQuantity() - childrenType.getMinQuantity() + 1);
                }
                // если в описание дочернего поля вложен объект
                if (childrenType.getObject() != null) {
                    for (int i = 0; i < count; i++) {
                        // создадим объект для генерации дочерних доменных объектов
                        DomainObject newDomainObject = dgsi.generateObject(templateList, childrenType.getObject(), domainObject);
                        newDomainObject = dgsi.addOfMissingFields(newDomainObject, domainObject);
                        domainObjects.add(newDomainObject);
                    }

                    AccessToken accessToken = accessControlService.createSystemAccessToken("GenerateChildrenField");
                    
                    // сохраним дочение доменные объекты
                    savedDomainObjects = domainObjectDao.save(domainObjects, accessToken);

                    // пройдемся по сохраненным объектам в цикле и добавим
                    // ссылки
                    // в родительский доменный объект на дочерние объекты
                    for (DomainObject savedDomainObject : savedDomainObjects) {
                        domainObject.setReference(childrenType.getObject().getType(), savedDomainObject);
                    }

                    // сохраним изменения в родительском объекте изменения
                   
                    domainObjectDao.save(domainObject, accessToken);
                }
            }
        }
    }
}
