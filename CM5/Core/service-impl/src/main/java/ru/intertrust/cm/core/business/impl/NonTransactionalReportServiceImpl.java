package ru.intertrust.cm.core.business.impl;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ReportServiceDelegate;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Нетранзакционная версия {@link ru.intertrust.cm.core.business.api.ReportServiceDelegate}
 * @author larin
 * 
 */
@Stateless(name = "NonTransactionalReportService")
@Local(ReportServiceDelegate.class)
@Remote(ReportServiceDelegate.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class NonTransactionalReportServiceImpl extends ReportServiceBaseImpl implements ReportServiceDelegate {

    @Override
    public ReportResult generate(String name, Map<String, Object> parameters) {
        return super.generate(name, parameters);
    }
    
    
    /**
     * Формирование отчета
     */
    @Override
    public ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays) {
        return super.generate(name, parameters, keepDays);
    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters) {
        ReportResult result = generate(name, parameters);
        return new AsyncResult<ReportResult>(result);
    }

}
