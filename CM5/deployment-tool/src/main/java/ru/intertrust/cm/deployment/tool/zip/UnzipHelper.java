package ru.intertrust.cm.deployment.tool.zip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by alexander on 01.08.16.
 */
public class UnzipHelper {

    private static Logger logger = LoggerFactory.getLogger(UnzipHelper.class);

    public boolean unzip(String srcDir, String targetDir, String zipName) {
        byte[] buffer = new byte[1024];

        File target = new File(targetDir);
        if (!target.exists()) {
            target.mkdir();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(srcDir + File.separator + zipName))) {
            ZipEntry ze = zis.getNextEntry();
            String nextFileName;

            while (ze != null) {
                nextFileName = ze.getName();
                File nextFile = new File(targetDir + File.separator + nextFileName);
                logger.info("Unpacking - {}", nextFile.getAbsolutePath());

                if (ze.isDirectory()) {
                    nextFile.mkdir();
                } else {
                    new File(nextFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(nextFile)) {
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            logger.info("Unpacking archive {} has been finished", zipName);
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
