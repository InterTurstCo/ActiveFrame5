package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceDelegate;
import ru.intertrust.cm.core.business.api.dto.ReportResult;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Имплементация сервиса генерации отчетов
 * @author larin
 * 
 */
@Stateless(name = "ReportService")
@Local(ReportService.class)
@Remote(ReportService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportServiceImpl extends ReportServiceBaseImpl implements ReportService {

    @Autowired
    @Qualifier("nonTransactionalReportService")
    private ReportServiceDelegate nonTransactionalReportService;

    public ReportResult generate(String name, Map<String, Object> parameters) {
        return generate(name, parameters, DataSourceContext.CLONE);
    }

    public ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays) {
        return generate(name, parameters, keepDays, DataSourceContext.CLONE);
    }

    @Override
    public ReportResult generate(String name, Map<String, Object> parameters, DataSourceContext dataSource) {
        return generate(name, parameters, null, dataSource);
    }

    /**
     * Формирование отчета
     */
    @Override
    public ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return super.generate(name, parameters, keepDays);
        } else {
            return nonTransactionalReportService.generate(name, parameters, keepDays);
        }
    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return generateAsync(name, parameters);
        } else {
            return nonTransactionalReportService.generateAsync(name, parameters);
        }
    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters) {
        ReportResult result = super.generate(name, parameters);
        return new AsyncResult<ReportResult>(result);
    }
}
