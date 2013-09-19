import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.interceptor.Interceptors;

/**
 * Класс который существует для выполнения инициализационных действий при старте приложения
 * Вызывает SpringBeanAutowiringInterceptor для гарантированного создания shared спринг-контекста см. {link beanRefContext.xml}
 */
@Startup
@Singleton
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class BootstrapBean {
}
