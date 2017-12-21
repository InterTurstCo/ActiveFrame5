package ru.intertrust.cm.plugins.permission;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PluginBase {
    private Logger logger;
    private StringBuffer log = new StringBuffer();
    
    public PluginBase(){
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected Map<String, String> getParametersMap(String param){
        Map<String, String> result = new HashMap<String, String>();
        if (param != null && !param.isEmpty()){
            String[] params = param.split("[ ;,]");
            for (String papamsItem : params) {
                String[] keyVal = papamsItem.trim().split("=");
                result.put(keyVal[0], keyVal[1]);
            }
        }
        return result;
    }
    
    protected void info(String message, Object ... params){
        String formatedMessage = null;
        if (params != null){
            formatedMessage = MessageFormat.format(message, params);
        }else{
            formatedMessage = message;
        }
        
        logger.info(formatedMessage);
        log.append(formatedMessage);
    }

    protected void debug(String message, Object ... params){
        String formatedMessage = null;
        if (params != null){
            formatedMessage = MessageFormat.format(message, params);
        }else{
            formatedMessage = message;
        }
        logger.debug(formatedMessage);
    }

    protected void error(String message, Throwable ex){
        logger.error(message, ex);
        log.append(message + "\n" + ExceptionUtils.getStackTrace(ex));
    }
    
    protected String getLog(){
        return log.toString();
    }
}
