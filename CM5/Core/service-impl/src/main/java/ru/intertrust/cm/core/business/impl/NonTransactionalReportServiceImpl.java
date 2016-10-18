package ru.intertrust.cm.core.business.impl;

import java.util.Map;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ReportServiceDelegate;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

/**
 * Нетранзакционная версия {@link ru.intertrust.cm.core.business.api.ReportServiceDelegate}
 * @author larin
 * 
 */
@Stateless(name = "NonTransactionalReportService")
@Local(ReportServiceDelegate.class)
@Remote(ReportServiceDelegate.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
