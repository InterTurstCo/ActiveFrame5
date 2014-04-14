import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

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

            //Загрузка контекста из файла beanRefContex.xml. Перенесено из интерсептора SpringBeanAutowiringInterceptor в этот метод для возможности установить таймаут транзакции
            ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(null);
            
            transaction.commit();
            logger.info("End create spring context");
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
}
