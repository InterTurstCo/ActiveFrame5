import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;

import ru.intertrust.cm.core.util.SingletonBeanFactoryLocator;

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
        try {
            logger.info("Initialized ejb context");
            //Загрузка контекста из файла beanRefContex.xml.
            final BeanFactory beanFactory = SingletonBeanFactoryLocator.getInstance().getBeanFactory(null);
        } catch (Throwable ex) {
            throw new FatalBeanException("Error init spring context", ex);
        }
    }
}
