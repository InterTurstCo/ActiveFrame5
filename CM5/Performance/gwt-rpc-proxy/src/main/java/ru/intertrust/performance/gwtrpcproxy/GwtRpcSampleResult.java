package ru.intertrust.performance.gwtrpcproxy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;

public class GwtRpcSampleResult extends HTTPSampleResult {
    private static final long serialVersionUID = 8100037991784822809L;
    private Object responseObject;
    
    public void setResponseObject(Object responseObject){
        this.responseObject = responseObject;
    }

    public void init(HTTPSampleResult originalResult) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class superClass = HTTPSampleResult.class;
        do {
            Field[] fromFields = superClass.getDeclaredFields();
            Object value = null;
            for (Field field : fromFields) {
                if (!Modifier.isStatic(field.getModifiers())){
                    field.setAccessible(true);
                    value = field.get(originalResult);
                    field.set(this, value);
                }
            }

            superClass = superClass.getSuperclass();
        } while (!superClass.equals(Object.class));
    }
    
    public Object getResponseObject(){
        return responseObject;
    }
}
