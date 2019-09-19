package ru.intertrust.performance.gwtrpcproxy.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.junit.Test;

import ru.intertrust.cm.core.business.api.dto.StringValue;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class TestJson {
    @Test
    public void  testParser(){
        StringValue str = new StringValue("\\2015\\09\\24\\355c1c00-2c84-4571-ad57-e343a8728684.png") ;
        Map args = new HashMap();
        args.put(JsonWriter.PRETTY_PRINT, true);

        String json = JsonWriter.objectToJson(str, args);
        
        System.out.println(json);
        Object res = JsonReader.jsonToJava(json);        
        Assert.assertTrue(str.equals(res));
    }
    
}
