package ru.intertrust.cm.performance.dataset.generatefields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.performance.dataset.DatasetGenerationServiceImpl;
import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
import ru.intertrust.cm.performance.dataset.xmltypes.ReferenceType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

public class GenerateReferenceField {
    protected String field;
    protected Id value;
    protected boolean ignore;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    public String getField() {
        return field;
    }

    public Id getValue() {
        return value;
    }

    public boolean ignoreField() {
        return ignore;
    }

    public void generateField(ReferenceType referenceType, List<TemplateType> templateList, String type,
            DatasetGenerationServiceImpl dgsi) throws IOException {
        // domainObjectDao = dod;

        if (referenceType.getName() != null) {
            field = referenceType.getName();
        } else {
            field = getNameFromTemplate(templateList, type);
        }

        if (referenceType.getType() != null) {

            List<DomainObject> domainObjectLinkArray = new ArrayList<DomainObject>();
            domainObjectLinkArray = dgsi.getDomainObjectList(referenceType.getType());

            // получаем размер листа с объектами на которые организуется
            // ссылка
            int listSize = domainObjectLinkArray.size();
            if (listSize > 0) {
                // получим случайным образом индекс, что бы по нему получить
                // объект из списка
                Random rnd = new Random(System.currentTimeMillis());
                int indexOfList = rnd.nextInt(listSize);

                // по индексу получим доменный объект на который будем ссылаться
                DomainObject dobj = domainObjectLinkArray.get(indexOfList);

                value = dobj.getId();
                ignore = false;
            } else {
                ignore = true;
            }
        } else {
            ignore = true;
        }
    }

    public void generateField(String referenceFieldName, String referenceFieldType, DatasetGenerationServiceImpl dgsi)
            throws IOException {
        // domainObjectDao = dod;
        field = referenceFieldName;

        List<DomainObject> domainObjectLinkArray = new ArrayList<DomainObject>();
        domainObjectLinkArray = dgsi.getDomainObjectList(referenceFieldType);

        // получаем размер листа с объектами на которые организуется
        // ссылка
        int listSize = domainObjectLinkArray.size();
        if (listSize > 0) {
            // получим случайным образом индекс, что бы по нему получить
            // объект из списка
            Random rnd = new Random(System.currentTimeMillis());
            int indexOfList = rnd.nextInt(listSize);

            // по индексу получим доменный объект на который будем ссылаться
            DomainObject dobj = domainObjectLinkArray.get(indexOfList);

            value = dobj.getId();
            ignore = false;
        } else {
            ignore = true;
        }

    }

    private String getNameFromTemplate(List<TemplateType> templateList, String type) {
        String name = "";
        boolean resultOk = false;
        for (TemplateType template : templateList) {
            // получаем объект из шаблона
            ObjectType object = template.getObject();
            // получим тип объекта
            String objectTemplateType = object.getType();
            // если тип у объекта есть
            if (objectTemplateType != null) {
                // сравниваем тип объекта из шаблона с типом объекта из сета
                if (objectTemplateType.equals(type)) {
                    // если типы совпали, то получим список полей объекта из
                    // шаблона
                    List<FieldType> fieldTypeList = object.getStringOrDateTimeOrReference();
                    for (FieldType field : fieldTypeList) {
                        // найдем поле с инструкциями для строки
                        if (field instanceof ReferenceType) {
                            ReferenceType referenceType = (ReferenceType) field;
                            name = referenceType.getName();
                            resultOk = true;
                        }
                    }
                }
            }
        }

        // если не нашли шаблона удовлетворяющего типу
        // переходим к поиску шаблона без типа
        if (!resultOk) {
            for (TemplateType template : templateList) {
                // получаем объект из шаблона
                ObjectType object = template.getObject();
                // получим тип объекта
                String objectTemplateType = object.getType();
                // если типа у объекта нету
                if (objectTemplateType == null) {
                    List<FieldType> fieldTypeList = object.getStringOrDateTimeOrReference();
                    for (FieldType field : fieldTypeList) {
                        // найдем поле с инструкциями для строки
                        if (field instanceof ReferenceType) {
                            ReferenceType referenceType = (ReferenceType) field;
                            name = referenceType.getName();
                        }
                    }
                }
            }
        }

        return name;
    }
}
