package ru.intertrust.cm.core.business.impl;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.report.ReportHelper;

/**
 * Устанавливает имя реального датасорса на котором будут выполняться запросы {@link ru.intertrust.cm.core.business.api.ReportService}
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
@Aspect
public class ReportsDataSourceSetter {

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

    @Autowired
    private ReportHelper reportHelper;

    @Around("this(ru.intertrust.cm.core.business.api.ReportService) && " +
            "execution(* *(String, ..)))")
     public Object setReportsServiceDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        if (currentDataSourceContext.get() != null) {
            return joinPoint.proceed();
        }

        Object lastParameter = joinPoint.getArgs()[joinPoint.getArgs().length - 1];
        if (lastParameter instanceof DataSourceContext) {
            DataSourceContext dataSource = (DataSourceContext) lastParameter;
            switch (dataSource) {
                case MASTER: {
                    try {
                        currentDataSourceContext.setToMaster();
                        return joinPoint.proceed();
                    } finally {
                        currentDataSourceContext.reset();
                    }
                }
                case CLONE: {
                    try {
                        currentDataSourceContext.setToReports();
                        return joinPoint.proceed();
                    } finally {
                        currentDataSourceContext.reset();
                    }
                }
            }
        }

        DomainObject reportDO = reportHelper.getReportTemplateObject((String) joinPoint.getArgs()[0]);
        Boolean forceMaster = reportDO.getBoolean("forceMaster");

        if (forceMaster != null && forceMaster) {
            try {
                currentDataSourceContext.setToMaster();
                return joinPoint.proceed();
            } finally {
                currentDataSourceContext.reset();
            }
        } else {
            try {
                currentDataSourceContext.setToReports();
                return joinPoint.proceed();
            } finally {
                currentDataSourceContext.reset();
            }
        }
    }
}
