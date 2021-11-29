package ru.intertrust.cm.core.dao.impl.attach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import ru.intertrust.cm.core.dao.api.BaseActionListener;
import ru.intertrust.cm.core.dao.api.UserTransactionService;


@Component ("immediateFileDeleteStrategy")
public class ImmediateFileDeleteStrategy implements FileDeleteStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ImmediateFileDeleteStrategy.class);

    @Autowired
    private UserTransactionService txService;

    @Override
    public void deleteFile(final String path) {
        txService.addListener(new BaseActionListener() {
            @Override
            public void onAfterCommit() {
                try {
                    Files.delete(Paths.get(path));
                    logger.info("Dereferred file " + path + " is deleted");
                } catch (IOException e) {
                    logger.warn("Failed to delete dereferred file " + path + ": " + e.getMessage());
                }
            }
        });
    }

    @Override
    public String toString() {
        return "ImmediateFileDeleteStrategy";
    }
}
