package ru.intertrust.cm.deployment.tool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Alexander Bogatyrenko on 28.07.16.
 * <p>
 * This class represents...
 */
public final class ProcessPrintUtil {

    private static Logger logger = LoggerFactory.getLogger(ProcessPrintUtil.class);

    private ProcessPrintUtil() {
        // nothing
    }

    public static void print(final Process process) {
        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
