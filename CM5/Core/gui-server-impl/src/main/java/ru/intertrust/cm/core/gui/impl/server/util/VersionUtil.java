package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
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
    private static String PRODUCT_VERSION = null;
    private static String ALIAS_PLATFORM_JAR = "dao-api.jar";

    public VersionUtil() {
    }

    public String getManifestInfo(String jarAlias, String locale) {
        final int endIndex = jarAlias.indexOf(".jar");
        if (endIndex == -1) {
            return null;
        }
        jarAlias = jarAlias.substring(0, endIndex);

        try {
            Enumeration<?> resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);

            while (resEnum.hasMoreElements()) {
                URL url = (URL) resEnum.nextElement();
                boolean isCoreVersionJar = url.getPath().contains(jarAlias);
                if (!isCoreVersionJar) {
                    continue;
                }
                try (InputStream is = url.openStream()) {
                    if (is != null) {
                        return getVersionFromManifest(is);
                    }
                } catch (Throwable e) {
                    throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys
                            .GUI_EXCEPTION_VERSION_ERROR, "Ошибка получения версии: ", locale), e);
                }
            }
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        return null;
    }

    public String getVersionFromManifest(InputStream is) throws IOException {
        Manifest manifest = new Manifest(is);
        Attributes mainAttribs = manifest.getMainAttributes();
        return mainAttribs.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
    }

    // При запуске из Idea в проекте платформы это работать не будет, потому что не собираются JAR-ы библиотек, а лишь компилируются
    public String getApplicationVersion(String locale) {
        if (PLATFORM_VERSION != null) {
            return PLATFORM_VERSION;
        }
        PLATFORM_VERSION = getManifestInfo(ALIAS_PLATFORM_JAR, locale);
        return PLATFORM_VERSION;
    }

    public String getProductVersion(String jarName, String locale) {
        if (PRODUCT_VERSION != null) {
            return PRODUCT_VERSION;
        }
        PRODUCT_VERSION = getManifestInfo(jarName, locale);
        return PRODUCT_VERSION;
    }
}
