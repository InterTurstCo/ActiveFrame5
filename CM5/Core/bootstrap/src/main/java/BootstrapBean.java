import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.*;

import java.lang.reflect.InvocationTargetException;

/**
 * Класс который существует для выполнения инициализационных действий при старте приложения Вызывает
 * SpringBeanAutowiringInterceptor для гарантированного создания shared спринг-контекста см. {link beanRefContext.xml}
 */
@Startup
@Singleton
//@RunAs("system")
@TransactionManagement(TransactionManagementType.BEAN)
public class BootstrapBean {
    private static final int DEFAULT_DEPLOY_TRANSACTION_TIMEOUT = 1000;
    private static final String DEPLOY_TRANSACTION_TIMEOUT = "deploy.transaction.timeout";
    private Logger logger = LoggerFactory.getLogger(BootstrapBean.class);
    
    @Resource
    private EJBContext ejbContext;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initialized ejb context");

            //Загрузка контекста из файла beanRefContex.xml.
            final BeanFactory beanFactory = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(null).getFactory();

            logger.info("Initialized spring context");

           // executeInitializationTasks(beanFactory);

            logger.info("Initial data loaded");
        } catch (Throwable ex) {
            throw new FatalBeanException("Error init spring context", ex);
        }
    }

    private void executeInitializationTasks(BeanFactory beanFactory) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        executeBeanMethod(beanFactory, "globallyLockableInitializer", "init");
    }

    private void executeBeanMethod(BeanFactory factory, String beanName, String methodName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Object scheduleTaskLoader = factory.getBean(beanName);
        scheduleTaskLoader.getClass().getMethod(methodName).invoke(scheduleTaskLoader);
    }
}
