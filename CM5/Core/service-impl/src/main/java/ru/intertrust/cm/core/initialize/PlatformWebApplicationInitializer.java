package ru.intertrust.cm.core.initialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import ru.intertrust.cm.core.business.impl.GloballyLockableInitializer;
import ru.intertrust.cm.core.util.SingletonBeanFactoryLocator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created by Vitaliy.Orlov on 04.05.2018.
 */
public abstract class PlatformWebApplicationInitializer implements WebApplicationInitializer {
    private Logger logger = LoggerFactory.getLogger(PlatformWebApplicationInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        try {
            logger.info("Initialized ejb context");

            //Загрузка контекста из файла beanRefContex.xml.
            final BeanFactory beanFactory = SingletonBeanFactoryLocator.getInstance().getBeanFactory(null);

            logger.info("Initialized spring context");

            GloballyLockableInitializer globallyLockableInitializer = beanFactory.getBean(GloballyLockableInitializer.class);

            logger.info("Start global initialize");
            globallyLockableInitializer.start();

            logger.info("Start app initialize");

            ApplicationContext applicationContext = onInitContext(servletContext, beanFactory);

            // Проверить не создан ли новый контекст при вызове onInitContext
            // Если не создан то создаем, если создан то проверяем его тип
            WebApplicationContext webApplicationContext = null;
            if (applicationContext != null && applicationContext instanceof WebApplicationContext) {
                webApplicationContext = (WebApplicationContext)applicationContext;
            }else{
                webApplicationContext = new XmlWebApplicationContext();
                ((XmlWebApplicationContext)webApplicationContext).setConfigLocation("META-INF/applicationContext.xml");
                if (applicationContext != null) {
                    ((XmlWebApplicationContext) webApplicationContext).setParent(applicationContext);
                }else{
                    ((XmlWebApplicationContext) webApplicationContext).setParent((ApplicationContext) beanFactory);
                }
            }

            servletContext.addListener(new ContextLoaderListener(webApplicationContext));

            logger.info("Finish app initialize");
            globallyLockableInitializer.finish();

            logger.info("Initial data loaded : complete");
        } catch (Throwable ex) {
            throw new FatalBeanException("Error init spring context", ex);
        }

    }

    /**
     * Инициализация контекста приложением. В данном методе необходимо произвести инициализацию дополнительного спринг контекста,
     * выполнить какие либо действия после инициализации.
     * Метод должен вернуть спринг контекст, являющийся дочерним по отношению к переданному контексту или null,
     * если никаких дополнительных инициализаций не требуется
     * Результат работы этого метода добавляется в ContextLoaderListener приложения как родительский
     * @param servletContext
     * @param platformBeanFactory
     * @return
     */
    public abstract ApplicationContext onInitContext(ServletContext servletContext, BeanFactory platformBeanFactory);
}
