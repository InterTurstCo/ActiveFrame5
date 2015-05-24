package ru.intertrust.cm.core.config.form.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.05.2015
 *         Time: 14:07
 */
public class ExtensionOperationStatus {
    private boolean inProcessing = false;
    private Map<String, String> idErrorMap = new LinkedHashMap<>();
    private List<String> successfulList = new ArrayList<>();

    public void putError(String id, String error){
        if(!successfulList.contains(id)){
            idErrorMap.put(id, error);
            inProcessing = true;
        }
    }
    public void addSuccessful(String id){
        successfulList.add(id);
        idErrorMap.remove(id);
        inProcessing = true;
    }
    public boolean isNotSuccessful(){
        return !inProcessing || !idErrorMap.isEmpty();
    }
    public String toErrorString(){
        StringBuilder sb = new StringBuilder();
        for (String id : idErrorMap.keySet()) {
            sb.append(idErrorMap.get(id)).append("\n");
        }
        return sb.toString();
    }

}
