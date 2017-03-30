package ru.intertrust.cm.core.business.api;

import java.util.Map;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

public interface ReportServiceAsync {
    /**
     * 
     * @param name
     * @param parameters
     * @param queue
     * @return
     */
    Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, Id queue, String ticket);    
}
