package ru.intertrust.performance.gwtscript;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Map;

public class ScriptExecutionContext {
    private Map variables = new Hashtable<String, Object>();
    private SecureRandom random = new SecureRandom();

    
    public void set(String name, Object value){
        variables.put(name, value);
    }
    
    public Object get(String name){
        return variables.get(name);
    }
    
    public String rndString(int length){
        return new BigInteger(length, random).toString(32);
    }
}
