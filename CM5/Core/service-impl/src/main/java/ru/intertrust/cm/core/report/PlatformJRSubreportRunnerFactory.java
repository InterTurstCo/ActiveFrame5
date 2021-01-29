package ru.intertrust.cm.core.report;

import net.sf.jasperreports.engine.fill.*;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.tools.SpringClient;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;

/**
 * Фабрика построителей подотчетов, в пуле потоков сервера приложений
 */
public class PlatformJRSubreportRunnerFactory extends ThreadPoolSubreportRunnerFactory {

    @Value("${managed.executor.service.lookup.name:java:comp/DefaultManagedExecutorService}")
    private String managedExecutorServiceLookupName = null;

    public PlatformJRSubreportRunnerFactory(){
        SpringClient.initAutowire(this);
    }

    @Override
    public JRSubreportRunner createSubreportRunner(JRFillSubreport fillSubreport, JRBaseFiller subreportFiller)
    {
        return new ThreadExecutorSubreportRunner(fillSubreport, subreportFiller, getAsExecutorService());
    }

    private ExecutorService getAsExecutorService(){
        try {
            return (ExecutorService)new InitialContext().lookup(managedExecutorServiceLookupName);
        } catch (NamingException ex) {
            throw new FatalException("Can not find Application server Executor Service", ex);
        }
    }

}