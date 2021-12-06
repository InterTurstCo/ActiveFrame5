package ru.intertrust.cm.core.business.impl;

import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.ReportParameterResolver;

import java.util.Map;

public class ReportParameterResolverImpl implements ReportParameterResolver {
    @Override
    public String resolve(String reportName, Map<String, Object> inParams, String paramName) {
        Object objParamValue = inParams != null ? inParams.get(paramName) : null;
        String paramValue = objParamValue != null ? objParamValue.toString() : paramName;
        return StringUtils.hasLength(paramValue) ? paramValue : "pdefault";
    }
}
