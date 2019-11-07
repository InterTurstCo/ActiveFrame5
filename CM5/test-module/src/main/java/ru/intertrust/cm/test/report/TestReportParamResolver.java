package ru.intertrust.cm.test.report;

import ru.intertrust.cm.core.business.api.ReportParameterResolver;

import java.util.Map;

public class TestReportParamResolver implements ReportParameterResolver {
    @Override
    public String resolve(String reportName, Map<String, Object> inParams, String paramName) {
        if (paramName.equals("test1")){
            return "test_1_value";
        }else {
            return inParams.get(paramName) != null ? inParams.get(paramName).toString() : "";
        }
    }
}
