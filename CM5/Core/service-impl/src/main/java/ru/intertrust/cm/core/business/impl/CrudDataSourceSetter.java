package ru.intertrust.cm.core.business.impl;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;

/**
 * Устанавливает имя реального датасорса на котором будут выполняться запросы {@link ru.intertrust.cm.core.business.api.CrudService}
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
@Aspect
public class CrudDataSourceSetter {

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Around("this(ru.intertrust.cm.core.business.api.CrudService) && " +
            "execution(* *(.., ru.intertrust.cm.core.business.api.DataSourceContext))")
    public Object setCrudServiceDataSourceContext(ProceedingJoinPoint joinPoint) throws Throwable {
        if (currentDataSourceContext.get() != null) {
            return joinPoint.proceed();
        }

        Object lastParameter = joinPoint.getArgs()[joinPoint.getArgs().length - 1];
        if (lastParameter instanceof DataSourceContext) {
            DataSourceContext dataSource = (DataSourceContext) lastParameter;
            if (DataSourceContext.CLONE.equals(dataSource)) {
                try {
                    currentDataSourceContext.setToCollections();
                    return joinPoint.proceed();
                } finally {
                    currentDataSourceContext.reset();
                }
            }
        }

        return joinPoint.proceed();
    }

}
