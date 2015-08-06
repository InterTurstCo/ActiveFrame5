package ru.intertrust.performance.jmetertools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class IdsMapper {
    private Map idsMap = new HashMap<Id, Id>();
    private static final Logger log = LoggingManager.getLoggerForClass();

    public void addToMap(Object savedResponce, Object realResponce) throws Exception, IllegalArgumentException, InvocationTargetException {
        if (savedResponce != null && realResponce != null) {
            Class superClass = savedResponce.getClass();
            do {
                Field[] fields = superClass.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);

                    if (Id.class.isAssignableFrom(field.getType())) {
                        addIdToMap(field, savedResponce, realResponce);
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
                    } else if (Dto.class.isAssignableFrom(field.getType()) && !field.getType().isEnum()) {
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

    private void addIdToMap(Field field, Object savedResponce, Object realResponce) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Id saveId = (Id) field.get(savedResponce);
        Id realId = (Id) field.get(realResponce);
        if (saveId != null && !saveId.equals(realId)) {
            idsMap.put(saveId, realId);
        }
    }

    public void replaceIdsInParams(Object[] parameters) throws Exception{
        for (Object parameter : parameters) {
            replaceIdInParam(parameter);
        }
    }

    private void replaceIdInParam(Object parameter) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Class superClass = parameter.getClass();
        do {
            Field[] fields = superClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (Id.class.isAssignableFrom(field.getType())) {
                    setField(parameter, field);
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    Map savedMap = (Map) field.get(parameter);
                    if (savedMap != null) {
                        for (Object key : savedMap.keySet()) {
                            Object value = savedMap.get(key);
                            if (Id.class.isAssignableFrom(value.getClass())){
                                if (idsMap.containsKey(value)){
                                    savedMap.put(key, idsMap.get(value));
                                }
                            }else{
                                replaceIdInParam(savedMap.get(key));
                            }
                        }
                    }
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection list = (Collection) field.get(parameter);
                    if (list != null) {
                        Object[] listAsArray = list.toArray();
                        boolean changed = false;
                        for (int i=0; i< listAsArray.length; i++) {
                            Object value = listAsArray[i];
                            if (Id.class.isAssignableFrom(value.getClass())){
                                if (idsMap.containsKey(value)){
                                    listAsArray[i] = idsMap.get(value);
                                    changed = true;
                                }
                            }else{
                                replaceIdInParam(value);
                            }
                        }
                        if (changed){
                            list.clear();
                            list.addAll(Arrays.asList(listAsArray));                        
                            field.set(parameter, list);
                        }
                    }

                } else if (Dto.class.isAssignableFrom(field.getType())) {
                    Dto savedObj = (Dto) field.get(parameter);
                    if (savedObj != null && !savedObj.getClass().isEnum()) {
                        //System.out.println(parameter + " " + savedObj + " " + field.getName());
                        replaceIdInParam(savedObj);
                    }
                }
            }
            superClass = superClass.getSuperclass();
        } while (!superClass.equals(Object.class));
    }

    private void setField(Object data, Field field) throws IllegalArgumentException, IllegalAccessException {
        Id savedId = (Id) field.get(data);
        if (idsMap.containsKey(savedId)) {
            field.set(data, idsMap.get(savedId));
            log.info("In field " + field + " replace " + savedId + " to " + idsMap.get(savedId));
        }
    }
}
