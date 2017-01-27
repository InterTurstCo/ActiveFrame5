package ru.intertrust.cm.test.extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.springframework.util.StreamUtils;

import ru.intertrust.cm.core.dao.api.extension.AfterGenerateReportExtentionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.model.FatalException;

@ExtensionPoint(filter="all-employee")
public class TestAfterGenerateReport implements AfterGenerateReportExtentionHandler {

    @Override
    public void onAfterGenerateReport(String name, Map<String, Object> parameters, File reportFile) {
        try {
            System.out.println("TestAfterGenerateReport Generated report " + reportFile.getName());
            if (parameters != null && parameters.containsKey("REPLACE_RESULT")) {
                System.out.println("Replace report " + reportFile.getName());
                try (FileOutputStream os = new FileOutputStream(reportFile)) {
                    InputStream is = getClass().getClassLoader().getResourceAsStream("reports/report-test-result/test.pdf");
                    StreamUtils.copy(is, os);
                }
            }
        } catch (Exception ex) {
            throw new FatalException("Error on TestAfterGenerateReport.onAfterGenerateReport", ex);
        }
    }

}
