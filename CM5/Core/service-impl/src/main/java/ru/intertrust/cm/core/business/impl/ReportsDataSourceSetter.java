package ru.intertrust.cm.core.business.impl;


import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.report.ReportHelper;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Устанавливает имя реального датасорса на котором будут выполняться запросы {@link ru.intertrust.cm.core.business.api.ReportService}
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
public class ReportsDataSourceSetter {
    @AroundInvoke
    public Object setReportsServiceDataSource(InvocationContext ctx) throws Throwable {
        CurrentDataSourceContext currentDataSourceContext = SpringApplicationContext.getContext().getBean(CurrentDataSourceContext.class);
        if (currentDataSourceContext.get() != null) {
            return ctx.proceed();
        }

        Object lastParameter = ctx.getParameters()[ctx.getParameters().length - 1];
        if (lastParameter instanceof DataSourceContext) {
            DataSourceContext dataSource = (DataSourceContext) lastParameter;
            switch (dataSource) {
                case MASTER: {
                    try {
                        currentDataSourceContext.setToMaster();
                        return ctx.proceed();
                    } finally {
                        currentDataSourceContext.reset();
                    }
                }
                case CLONE: {
                    try {
                        currentDataSourceContext.setToReports();
                        return ctx.proceed();
                    } finally {
                        currentDataSourceContext.reset();
                    }
                }
            }
        }

        ReportHelper reportHelper = SpringApplicationContext.getContext().getBean(ReportHelper.class);
        DomainObject reportDO = reportHelper.getReportTemplateObject(((ReportMetadataConfig) ctx.getParameters()[0]).getName());
        Boolean forceMaster = reportDO.getBoolean("forceMaster");

        if (forceMaster != null && forceMaster) {
            try {
                currentDataSourceContext.setToMaster();
                return ctx.proceed();
            } finally {
                currentDataSourceContext.reset();
            }
        } else {
            try {
                currentDataSourceContext.setToReports();
                return ctx.proceed();
            } finally {
                currentDataSourceContext.reset();
            }
        }
    }
}
