package ru.intertrust.cm.core.rest.api;

import java.text.SimpleDateFormat;

public class ReportParam {
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String VALUE = "value";
    public static final String PARAMETERS = "parameters";


    public enum ParamTypes{
        String,
        Int,
        Long,
        Double,
        Boolean,
        List,
        DateTime,
        Id
    }

    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
}
