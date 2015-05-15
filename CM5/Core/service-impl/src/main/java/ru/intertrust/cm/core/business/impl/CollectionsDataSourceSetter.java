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
 * Устанавливает имя реального датасорса на котором будут выполняться запросы {@link ru.intertrust.cm.core.business.api.CollectionsService}
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
@Aspect
public class CollectionsDataSourceSetter {

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Around("this(ru.intertrust.cm.core.business.api.CollectionsService) && " +
            "(execution(* findCollection(..)) || execution(* isCollectionEmpty(..)) || execution(* findCollectionCount(..)))")
     public Object setCollectionsServiceDataSourceContext(ProceedingJoinPoint joinPoint) throws Throwable {
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
                        currentDataSourceContext.setToCollections();
                        return joinPoint.proceed();
                    } finally {
                        currentDataSourceContext.reset();
                    }
                }
            }
        }

        String collectionName = (String) joinPoint.getArgs()[0];
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if (collectionConfig.isUseClone()) {
            try {
                currentDataSourceContext.setToCollections();
                return joinPoint.proceed();
            } finally {
                currentDataSourceContext.reset();
            }
        }

        return joinPoint.proceed();
    }

    @Around("this(ru.intertrust.cm.core.business.api.CollectionsService) && " +
            "execution(* findCollectionByQuery(.., ru.intertrust.cm.core.business.api.DataSourceContext))")
    public Object setCollectionsServiceByQueryDataSourceContext(ProceedingJoinPoint joinPoint) throws Throwable {
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
                        currentDataSourceContext.setToCollections();
                        return joinPoint.proceed();
                    } finally {
                        currentDataSourceContext.reset();
                    }
                }
            }
        }

        return joinPoint.proceed();
    }
}
