package ru.intertrust.cm.core.report;

import net.sf.jasperreports.engine.fill.JRBaseFiller;
import net.sf.jasperreports.engine.fill.JRFillSubreport;
import net.sf.jasperreports.engine.fill.ThreadExecutorSubreportRunner;

import java.util.concurrent.Executor;

/**
 * Задача, в которой выполняется построение подотчетов в отдельном потоке.
 * Переопределеяет classLoader чтоб работали скриптлеты
 */
public class PlatformThreadExecutorSubreportRunner extends ThreadExecutorSubreportRunner {
    private ClassLoader mainReportClassLoader;

    public PlatformThreadExecutorSubreportRunner(JRFillSubreport fillSubreport, JRBaseFiller subreportFiller,
                                                 Executor threadExecutor, ClassLoader classLoader) {
        super(fillSubreport, subreportFiller, threadExecutor);
        this.mainReportClassLoader = classLoader;
    }

    @Override
    public void run(){
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mainReportClassLoader);
            super.run();
        }finally {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
        }
    }
}
