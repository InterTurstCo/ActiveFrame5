package ru.intertrust.cm.remoteclient.report.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ru.intertrust.cm.core.business.api.dto.ReportResult;

public class MultyThreadReportTest extends TestReportService {

    public static void main(String[] args) {
        try {
            MultyThreadReportTest test = new MultyThreadReportTest();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            long start = System.currentTimeMillis();

            execute(args, null);
            initServices();

            deployReport("../reports/reports/all-employee");
            Map params = new HashMap();
            //Первый отчет прогоняем в одном потоке, чтоб выкачался в кэш
            generateReport("all-employee", params, UUID.randomUUID().toString() + "_");
            
            //Запускам массив потоков
            List<Thread> threads = new ArrayList<Thread>();
            for (int i = 0; i < 1000; i++) {
                Thread generateReportThread = new Thread(new GenerateReport());
                generateReportThread.start();
                threads.add(generateReportThread);
                Thread.currentThread().sleep(10);
            }

            boolean started = true;
            while (started) {
                started = false;
                for (Thread thread : threads) {
                    if (thread.isAlive()) {
                        started = true;
                        break;
                    }
                }
                log("Sleep 1 sec");
                Thread.currentThread().sleep(1000);
            }

            log("Test complete at " + (System.currentTimeMillis() - start));
        } finally {
            writeLog();
        }
    }

    public class GenerateReport implements Runnable {

        @Override
        public void run() {
            try {
                Map params = new HashMap();
                ReportResult result = generateReport("all-employee", params, UUID.randomUUID().toString() + "_");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
