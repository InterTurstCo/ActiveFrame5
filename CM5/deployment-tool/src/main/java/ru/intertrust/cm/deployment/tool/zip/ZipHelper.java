package ru.intertrust.cm.deployment.tool.zip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Created by alexander on 01.08.16.
 */
public class ZipHelper {

    private static Logger logger = LoggerFactory.getLogger(ZipHelper.class);

    private String srcDir;
    private String targetDir;

    public ZipHelper(String srcDir, String targetDir) {
        this.srcDir = srcDir;
        this.targetDir = targetDir;
    }

    public boolean zip(String zipName) {
        List<String> files = getFiles(new File(srcDir));
        if (!isEmpty(files)) {
            byte[] buffer = new byte[1024];
            try(FileOutputStream fos = new FileOutputStream(targetDir + File.separator + zipName);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
                logger.info("Start creating archive with name {}", zipName);

                for (final String file : files) {
                    logger.info("File processing - {}", file);
                    ZipEntry ze = new ZipEntry(file);
                    zos.putNextEntry(ze);

                    if ((file.substring(file.length() - 1)).equals(File.separator)) {
                        continue;
                    }

                    try (FileInputStream in = new FileInputStream(srcDir + File.separator + file)) {
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                }
                zos.closeEntry();
                logger.info("Creating archive {} has been finished", zipName);
                return true;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    private List<String> getFiles(File file) {
        List<String> files = new ArrayList<>();
        if (file.isFile()) {
            files.add(getRelativePath(file.getAbsolutePath()));
        } else if (file.isDirectory()) {
            String dir = file.getAbsoluteFile().toString();
            if (!dir.equalsIgnoreCase(srcDir)) {
                files.add(dir.substring(srcDir.length() + 1) + File.separator);
            }
            for (String nextFile : file.list()) {
                files.addAll(getFiles(new File(file, nextFile)));
            }
        }
        return files;
    }

    private String getRelativePath(final String filename) {
        return filename.substring(srcDir.length() + 1);
    }
}
