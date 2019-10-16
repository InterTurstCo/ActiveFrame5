package ru.intertrust.cm.core.business.impl;


import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Устанавливает имя реального датасорса на котором будут выполняться запросы {@link ru.intertrust.cm.core.business.api.CollectionsService}
 * @author vmatsukevich
 *         Date: 11/12/13
 *         Time: 3:04 PM
 */
public class CollectionsDataSourceSetter {

    @AroundInvoke
    public Object setCollectionsServiceDataSourceContext(InvocationContext joinPoint) throws Throwable {
        CurrentDataSourceContext currentDataSourceContext = SpringApplicationContext.getContext().getBean(CurrentDataSourceContext.class);
        ConfigurationExplorer configurationExplorer = SpringApplicationContext.getContext().getBean(ConfigurationExplorer.class);
        if (currentDataSourceContext.get() != null) {
            return joinPoint.proceed();
        }

        Object lastParameter = joinPoint.getParameters()[joinPoint.getParameters().length - 1];
        if (lastParameter instanceof DataSourceContext) {
            DataSourceContext dataSource = (DataSourceContext) lastParameter;
            if(dataSource.equals(DataSourceContext.MASTER)) {
                try {
                    currentDataSourceContext.setToMaster();
                    return joinPoint.proceed();
                } finally {
                    currentDataSourceContext.reset();
                }
            }else if(dataSource.equals(DataSourceContext.CLONE)){
                    try {
                        currentDataSourceContext.setToCollections();
                        return joinPoint.proceed();
                    } finally {
                        currentDataSourceContext.reset();
                    }
                }
            }

        if (joinPoint.getMethod().getName().contains("ByQuery")) {
            return joinPoint.proceed();
        }
        String collectionName = (String) joinPoint.getParameters()[0];
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
}
