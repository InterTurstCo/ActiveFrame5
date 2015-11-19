package ru.intertrust.performance.jmetertools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class DomainObjectMapper {
    private Map doMap = new HashMap<Id, DomainObject>();

    private static final Logger log = LoggingManager.getLoggerForClass();

    public void addToMap(Object savedResponce, Object realResponce) throws Exception, IllegalArgumentException, InvocationTargetException {
        if (savedResponce != null && realResponce != null) {
            Class superClass = savedResponce.getClass();
            do {
                Field[] fields = superClass.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);

                    if (DomainObject.class.isAssignableFrom(field.getType())) {
                        addDomainObjectToMap(field, savedResponce, realResponce);
                    } else if (Map.class.isAssignableFrom(field.getType())) {
                        Map savedMap = (Map) field.get(savedResponce);
                        Map realMap = (Map) field.get(realResponce);
                        if (savedMap != null) {
                            for (Object key : savedMap.keySet()) {
                                addToMap(savedMap.get(key), realMap.get(key));
                            }
                        }
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        //С массивом не знаю что делать ( могут и разное кол во записей вернутся может и порядок изменится
                    } else if (Dto.class.isAssignableFrom(field.getType()) && !field.getType().isEnum() && !Modifier.isStatic(field.getModifiers())) {
                        Dto savedObj = (Dto) field.get(savedResponce);
                        Dto realObj = (Dto) field.get(realResponce);
                        //System.err.println(savedResponce + "." + field.getName() + "=" + savedObj);
                        if (savedObj != null) {
                            addToMap(savedObj, realObj);
                        }
                    }
                }
                superClass = superClass.getSuperclass();
            } while (!superClass.equals(Object.class));
        }
    }

    private void addDomainObjectToMap(Field field, Object savedResponce, Object realResponce) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        DomainObject saveDo = (DomainObject) field.get(savedResponce);
        DomainObject realDo = (DomainObject) field.get(realResponce);
        if (saveDo != null && saveDo.getId() != null) {
            doMap.put(saveDo.getId(), realDo);
        }
    }

    public void replaceIdsInParams(Object[] parameters) throws IllegalArgumentException, IllegalAccessException {
        for (Object parameter : parameters) {
            replaceIdInParam(parameter);
        }
    }

    private void replaceIdInParam(Object parameter) throws IllegalArgumentException, IllegalAccessException {
        if (parameter == null){
            return;
        }
        
        Class superClass = parameter.getClass();
        do {
            Field[] fields = superClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (DomainObject.class.isAssignableFrom(field.getType())) {
                    setField(parameter, field);
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    Map savedMap = (Map) field.get(parameter);
                    if (savedMap != null) {
                        for (Object key : savedMap.keySet()) {
                            replaceIdInParam(savedMap.get(key));
                        }
                    }
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection list = (Collection) field.get(parameter);
                    if (list != null) {
                        for (Object item : list) {
                            replaceIdInParam(item);
                        }
                    }

                } else if (Dto.class.isAssignableFrom(field.getType())) {
                    Dto savedObj = (Dto) field.get(parameter);
                    if (savedObj != null && !savedObj.getClass().isEnum() && !Modifier.isStatic(field.getModifiers())) {
                        //System.out.println(parameter + " " + savedObj + " " + field.getName());
                        replaceIdInParam(savedObj);
                    }
                }
            }
            superClass = superClass.getSuperclass();
        } while (!superClass.equals(Object.class));
    }

    private void setField(Object data, Field field) throws IllegalArgumentException, IllegalAccessException {
        DomainObject savedDo = (DomainObject) field.get(data);
        if (savedDo != null){
            if (doMap.containsKey(savedDo.getId())) {
                field.set(data, doMap.get(savedDo.getId()));
                log.info("Replace with id " + savedDo.getId() + " to " + ((DomainObject) doMap.get(savedDo.getId())).getId());
            }
        }
    }
}
