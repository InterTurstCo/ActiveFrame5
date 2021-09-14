package ru.intertrust.cm.core.business.report;

import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.ReportParameterResolver;
import ru.intertrust.cm.core.business.impl.ReportResultBuilder;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestReportBuilder {

    @InjectMocks
    private ReportResultBuilder builder = new ReportResultBuilder();

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private CurrentUserAccessor currentUserAccessor;

    @Before
    public void init(){
        when(currentUserAccessor.getCurrentUser()).thenReturn("user1");

        when(applicationContext.getBean(anyString())).thenReturn(new ReportParameterResolver(){
            @Override
            public String resolve(String reportName, Map<String, Object> inParams, String paramName){
                if (paramName.equals("param4")){
                    return "xxx";
                }else {
                    return inParams.get(paramName) != null ? inParams.get(paramName).toString() : "";
                }
            }
        });
    }

    @Test
    public void testResultFileName() {
        Map<String, Object> params = new HashMap<>();
        ReportMetadataConfig config = new ReportMetadataConfig();
        config.setName("report1");
        config.setDescription("report1 description");

        // InBox подстановки
        config.setFileNameMask("test_{name}_{description}_{creator}");
        String result = (String) ReflectionTestUtils.invokeMethod(builder, "getReportName", config, "pdf", params);
        assertEquals(result, "test_report1_report1 description_user1.pdf");

        config.setFileNameMask("test_{short-date}");
        result = (String) ReflectionTestUtils.invokeMethod(builder, "getReportName", config, "pdf", params);
        assertTrue(result.matches("test_\\d\\d-\\d\\d-\\d\\d\\d\\d.pdf"));

        config.setFileNameMask("test_{long-date}");
        result = (String) ReflectionTestUtils.invokeMethod(builder, "getReportName", config, "pdf", params);
        assertTrue(result.matches("test_\\d\\d-\\d\\d-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d.pdf"));

        // Подстановки из параметров
        config.setFileNameMask("test_{P$param1}_{P$param2}_{P$param.with.point.3}_{P$param4}");
        config.setReportParameterResolver("testParamResolver");
        params.put("param1", "param_1_value");
        params.put("param2", "param_2_value");
        params.put("param.with.point.3", "param_3_value");
        params.put("param4", "need_not_use");
        result = (String) ReflectionTestUtils.invokeMethod(builder, "getReportName", config, "pdf", params);
        assertEquals(result, "test_param_1_value_param_2_value_param_3_value_xxx.pdf");

        // Смешанные подстановки (встроенные и из параметров)
        config.setFileNameMask("test_{P$param1}_{name}_{P$param.with.point.3}");
        result = (String) ReflectionTestUtils.invokeMethod(builder, "getReportName", config, "pdf", params);
        assertEquals(result, "test_param_1_value_report1_param_3_value.pdf");
    }
}
