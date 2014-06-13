import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import java.lang.reflect.InvocationTargetException;

/**
 * Класс который существует для выполнения инициализационных действий при старте приложения Вызывает
 * SpringBeanAutowiringInterceptor для гарантированного создания shared спринг-контекста см. {link beanRefContext.xml}
 */
@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class BootstrapBean {
    private static final int DEFAULT_DEPLOY_TRANSACTION_TIMEOUT = 1000;
    private static final String DEPLOY_TRANSACTION_TIMEOUT = "deploy.transaction.timeout";
    private Logger logger = LoggerFactory.getLogger(BootstrapBean.class);
    
    @Resource
    private EJBContext ejbContext;

    @PostConstruct
    public void init() {
        UserTransaction transaction = ejbContext.getUserTransaction();
        try {
            //Установка таймаута транзакции при загрузке приложения
            String timeoutValue = System.getProperty(DEPLOY_TRANSACTION_TIMEOUT);
            int timeout = 0;
            if (timeoutValue != null){
                timeout = Integer.parseInt(timeoutValue);
            }else{
                timeout = DEFAULT_DEPLOY_TRANSACTION_TIMEOUT;
            }
            logger.info("Start create spring context. Transaction timeout = " + timeout);
            
            transaction.setTransactionTimeout(timeout);
            transaction.begin();

            logger.info("Initialized ejb context");

            //Загрузка контекста из файла beanRefContex.xml. Перенесено из интерсептора SpringBeanAutowiringInterceptor в этот метод для возможности установить таймаут транзакции
            final BeanFactory beanFactory = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(null).getFactory();

            logger.info("Initialized spring context");

            loadConfiguration(beanFactory);
            initDomainObjectTypeIdCache(beanFactory);
            loadInitialData(beanFactory);
            importSystemData(beanFactory);
            importReportsData(beanFactory);
            initializeScheduledTasks(beanFactory);

            transaction.commit();

            logger.info("Initial data loaded");
        } catch (Throwable ex) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignoreEx) {
                }
            }
            throw new FatalBeanException("Error init spring context", ex);
        }
    }

    private void loadConfiguration(BeanFactory beanFactory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        executeBeanMethod(beanFactory, "configurationLoader", "load");
    }

    private void initDomainObjectTypeIdCache(BeanFactory beanFactory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        executeBeanMethod(beanFactory, "domainObjectTypeIdCache", "build");
    }

    private void loadInitialData(BeanFactory beanFactory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        executeBeanMethod(beanFactory, "initialDataLoader", "load");
    }

    private void importSystemData(BeanFactory beanFactory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        executeBeanMethod(beanFactory, "importSystemData", "onLoad");
    }

    private void importReportsData(BeanFactory beanFactory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        executeBeanMethod(beanFactory, "importReportsData", "onLoad");
    }

    private void initializeScheduledTasks(BeanFactory beanFactory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        executeBeanMethod(beanFactory, "sheduleTaskLoader", "initialize");
    }

    private void executeBeanMethod(BeanFactory factory, String beanName, String methodName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Object scheduleTaskLoader = factory.getBean(beanName);
        scheduleTaskLoader.getClass().getMethod(methodName).invoke(scheduleTaskLoader);
    }
}
