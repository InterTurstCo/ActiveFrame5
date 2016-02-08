package ru.intertrust.performance.jmetertools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import ru.intertrust.cm.core.business.api.dto.Dto;


public class UploadMapper {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private Map uploadMap = new HashMap<String, String>();

    public void addToMap(String savedResponce, String realResponce) {
        String saved = savedResponce.split(",")[0];
        String real = realResponce.split(",")[0];
        uploadMap.put(saved, real);
        log.info("Add file to upload map. Saved=" + saved + "; Runtime=" + real);
    }

    public void replaceUploadResult(Object[] parameters) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
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
                if (String.class.isAssignableFrom(field.getType())) {
                    setField(parameter, field);
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    Map savedMap = (Map) field.get(parameter);
                    if (savedMap != null) {
                        for (Object key : savedMap.keySet()) {
                            Object value = savedMap.get(key);
                            if (value != null){
                                if (String.class.isAssignableFrom(value.getClass())){
                                    if (uploadMap.containsKey(value)){
                                        savedMap.put(key, uploadMap.get(value));
                                    }
                                }else{
                                    replaceIdInParam(savedMap.get(key));
                                }
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
                            if (value != null){
                                if (String.class.isAssignableFrom(value.getClass())){
                                    if (uploadMap.containsKey(value)){
                                        listAsArray[i] = uploadMap.get(value);
                                        changed = true;
                                    }
                                }else{
                                    replaceIdInParam(value);
                                }
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
        String savedFile = (String) field.get(data);
        if (uploadMap.containsKey(savedFile)) {
            field.set(data, uploadMap.get(savedFile));
            log.info("Upload. In field " + field + " replace " + savedFile + " to " + uploadMap.get(savedFile));
        }
    }

    public void clear() {
        uploadMap.clear();        
    }
}
