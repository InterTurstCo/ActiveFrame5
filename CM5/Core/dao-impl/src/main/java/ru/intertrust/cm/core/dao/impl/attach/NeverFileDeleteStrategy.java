package ru.intertrust.cm.core.dao.impl.attach;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component ("neverFileDeleteStrategy")
public class NeverFileDeleteStrategy implements FileDeleteStrategy {

    private static final Logger logger = LoggerFactory.getLogger(NeverFileDeleteStrategy.class);

    @Override
    public void deleteFile(String path) {
        if (logger.isInfoEnabled()) {
            logger.info("File " + path + " is not referred anymore");
        }
    }

    @Override
    public String toString() {
        return "NeverFileDeleteStrategy";
    }
}
