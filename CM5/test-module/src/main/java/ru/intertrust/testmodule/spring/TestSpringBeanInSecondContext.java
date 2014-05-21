package ru.intertrust.testmodule.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSpringBeanInSecondContext {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionPointUseSecondContextBean.class);

    public void testMethod() {
        logger.info("Execute test method");
    }

}
