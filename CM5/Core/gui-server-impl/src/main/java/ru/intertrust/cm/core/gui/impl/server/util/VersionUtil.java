package ru.intertrust.cm.core.gui.impl.server.util;

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

    private static String APPLICATION_VERSION = "7.7.77-77-SNAPSHOT";

    public VersionUtil() {
    }

    private String getManifestInfo() {
        Enumeration resEnum;
        try {
            resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            //resEnum = getClass().getClassLoader().getResources(JarFile.MANIFEST_NAME);

            while (resEnum.hasMoreElements()) {
                try {
                    URL url = (URL) resEnum.nextElement();
                    InputStream is = url.openStream();
                    if (is != null) {
                        Manifest manifest = new Manifest(is);
                        Attributes mainAttribs = manifest.getMainAttributes();
                        String version = mainAttribs.getValue("Implementation-Version");

                        if (version != null && mainAttribs.getValue("Implementation-Title").equals("dao-api")) {
                            return version;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        return null;
    }

    public String getApplicationVersion() {
        if (getManifestInfo()!= null){
            APPLICATION_VERSION = getManifestInfo();
        }
        return APPLICATION_VERSION;
    }

    public void setApplicationVersion(String APPLICATION_VERSION) {
        this.APPLICATION_VERSION = APPLICATION_VERSION;
    }
}
