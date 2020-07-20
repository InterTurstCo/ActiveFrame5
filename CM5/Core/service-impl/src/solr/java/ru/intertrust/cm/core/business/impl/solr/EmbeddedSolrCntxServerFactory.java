package ru.intertrust.cm.core.business.impl.solr;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

public abstract class EmbeddedSolrCntxServerFactory {
    private static final Logger logger = Logger.getLogger(EmbeddedSolrCntxServerFactory.class);
    private static final String CNTX_PREFIX = "cntx-conf";
    private static final String ORIG_CORE_CONFIG = "solr-cntx.xml";
    private static final String CORE_CONFIG = "solr.xml";
    private static final String DATA_DIR_PLACEHOLDER = "_data-dir_";
    private static final String INST_DIR_PLACEHOLDER = "_inst-dir_";
    private static final int BUF_SIZE = 8192;

    public static SolrServer getSolrServer(String homeDir, String dataDir) {
        try {
            createDirIfNotExists(homeDir);
            createDirIfNotExists(dataDir);
            File configFile = new File(copyConfigAndReturnFileName(homeDir, dataDir));
            copyResources(homeDir);
            CoreContainer container = CoreContainer.createAndLoad(homeDir, configFile);
            Collection<String> coreNames = container.getAllCoreNames();
            return coreNames != null && !coreNames.isEmpty() ?
                    new EmbeddedSolrServer(container, coreNames.iterator().next()) : null;
        } catch (Exception e) {
            logger.error("Error creating embedded solr server.", e);
        }
        return null;
    }

    private static String copyConfigAndReturnFileName(String homeDir, String dataDir) throws IOException {
        String fileName = homeDir + File.separator + CORE_CONFIG;
        if (!checkIfExists(fileName)) {
            String coreConfig = getCoreConfig();
            coreConfig = coreConfig.replaceAll(DATA_DIR_PLACEHOLDER, dataDir).
                    replaceAll(INST_DIR_PLACEHOLDER, homeDir);
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    new FileOutputStream(fileName), "utf-8")) {
                outputStreamWriter.write(coreConfig);
                outputStreamWriter.flush();
            }
        }
        return fileName;
    }

    private static void copyResources(String homeDir) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/" + CNTX_PREFIX + "/**/*");
        for (Resource res : resources) {
            copyFromResource(res, homeDir);
        }
    }

    private static String getCoreConfig() throws IOException {
        StringBuffer sb = new StringBuffer();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/" + CNTX_PREFIX + "/" + ORIG_CORE_CONFIG);
        if (resources != null && resources.length > 0) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(resources[0].getInputStream(), "utf-8")) {
                char buffer[] = new char[BUF_SIZE];
                int readCnt = 0;
                while ((readCnt = inputStreamReader.read(buffer)) > 0) {
                    sb.append(buffer, 0, readCnt);
                }
            }
        }
        return sb.toString();
    }

    private static boolean checkIfExists(String path) {
        File fileObj = new File(path != null ? path : "");
        return fileObj.exists();
    }

    private static void createDirIfNotExists(String path) throws IOException {
        if (!checkIfExists(path)) {
            Files.createDirectories(Paths.get(path != null ? path : ""));
        }
    }

    private static void copyFromResource(Resource srcResource, String dstDir) throws IOException {
        File srcFile = srcResource.getFile();
        if (srcFile != null) {
            String srcPath = srcFile.getPath();
            int pos = srcPath.indexOf(CNTX_PREFIX);
            if (pos >= 0) {
                srcPath = srcPath.substring(pos + (CNTX_PREFIX + File.separator).length());
            }
            String dstPath = dstDir + File.separator + srcPath;
            if (!checkIfExists(dstPath)) {
                if (srcFile.isDirectory()) {
                    // создаем каталог
                    createDirIfNotExists(dstPath);
                } else if (srcFile.isFile()) {
                    // копируем файл
                    try (InputStream inputStream = srcResource.getInputStream();
                         OutputStream outputStream = new FileOutputStream(dstPath)) {
                        copy(inputStream, outputStream);
                    }
                }
            }
        }
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        if (inputStream != null && outputStream != null) {
            byte buffer[] = new byte[BUF_SIZE];
            int readCnt = 0;
            while ((readCnt = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readCnt);
            }
        }
    }
}
