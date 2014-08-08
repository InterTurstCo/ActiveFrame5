package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.gui.model.GuiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Created by tbilyi on 07.08.2014.
 */
public class VersionUtil {

    private static String PLATFORM_VERSION = null;

    public VersionUtil() {
    }

    private String getManifestInfo() {
        Enumeration resEnum;
        try {
            resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            //resEnum = getClass().getClassLoader().getResources(JarFile.MANIFEST_NAME);

            while (resEnum.hasMoreElements()) {
                URL url = (URL) resEnum.nextElement();
                boolean isCoreVersionJar = url.getPath().contains("dao-api-");
                System.out.println(url.getPath());
                if (!isCoreVersionJar) {
                    continue;
                }
                InputStream is = url.openStream();
                try {
                    if (is != null) {
                        Manifest manifest = new Manifest(is);
                        Attributes mainAttribs = manifest.getMainAttributes();
                        return mainAttribs.getValue("Implementation-Version");
                    }
                } catch (Throwable e) {
                    throw new GuiException(e);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        return null;
    }

    // При запуске из Idea в проекте платформы это работать не будет, потому что не собираются JAR-ы библиотек, а лишь компилируются
    public String getApplicationVersion() {
        if (PLATFORM_VERSION != null) {
            return PLATFORM_VERSION;
        }
        PLATFORM_VERSION = getManifestInfo();
        return PLATFORM_VERSION;
    }
}
