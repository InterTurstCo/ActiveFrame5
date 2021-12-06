package ru.intertrust.cm.core.business.impl;

import java.io.File;
import java.io.InputStream;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class ReportServiceAdminImplTest {

    @Test
    public void compileJasper() {
        try {
            File file = new File("src/test/resources/reports/test.jrxml");
            ReflectionTestUtils.invokeMethod(new ReportServiceAdminImpl(), "compileJasper", file);
        } finally {
            assertTrue(new File("src/test/resources/reports/test.jasper").delete());
        }
    }

}