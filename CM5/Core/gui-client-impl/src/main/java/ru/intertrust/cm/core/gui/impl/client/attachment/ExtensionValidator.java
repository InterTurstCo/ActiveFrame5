package ru.intertrust.cm.core.gui.impl.client.attachment;

import com.google.gwt.dom.client.Element;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class ExtensionValidator {
    private List<String> extensions;
    private List<String> mimeTypes;

    private static final List<String> IMAGES_EXTENSIONS = Arrays.asList(".gif", ".jpeg", ".jpg", ".jpe", ".jfif",
            ".png", ".tif", ".tiff", ".bmp", ".dib", ".xbm", ".ico", ".cur");

    private static final List<String> IMAGES_MIME_TYPE = Arrays.asList("image/gif", "image/jpeg", "image/pjpeg",
            "image/png", "image/tiff", "image/vnd.microsoft.icon", "image/bmp", "image/x-bmp",
            "image/x-bitmap", "image/x-xbitmap", "image/x-win-bitmap", "image/x-windows-bmp", "image/ms-bmp",
            "image/x-ms-bmp", "application/bmp", "application/x-bmp", "application/x-win-bitmap");

    public ExtensionValidator(AcceptedTypesConfig acceptedTypesConfig, boolean imagesOnly) {
        extensions = initExtensions(acceptedTypesConfig, imagesOnly);
        mimeTypes = initMimeTypes(acceptedTypesConfig, imagesOnly);
    }

    private List<String> initExtensions(AcceptedTypesConfig acceptedTypesConfig, boolean imagesOnly) {
        if (acceptedTypesConfig == null) {
            return imagesOnly ? IMAGES_EXTENSIONS : null;
        }
        List<String> extensions = new ArrayList<String>();
        List<AcceptedTypeConfig> acceptedTypeConfigs = acceptedTypesConfig.getAcceptedTypeConfigs();
        for (AcceptedTypeConfig acceptedTypeConfig : acceptedTypeConfigs) {
            List<String> extensionsForMimeType = getExtensionsForMimeType(acceptedTypeConfig);
            extensions.addAll(extensionsForMimeType);
        }
        if (imagesOnly) {
            extensions.retainAll(IMAGES_EXTENSIONS);
        }
        return extensions;
    }

    private List<String> getExtensionsForMimeType(AcceptedTypeConfig acceptedTypeConfig) {
        String extensionsCommaSeparated = acceptedTypeConfig.getExtensions();
        String[] extensionsArr = extensionsCommaSeparated.split(",");
        List<String> extensions = new ArrayList<>();
        for (String extension : extensionsArr) {
            extensions.add(extension.trim());
        }
        return extensions;
    }

    private List<String> initMimeTypes(AcceptedTypesConfig acceptedTypesConfig, boolean imagesOnly) {
        if (acceptedTypesConfig == null) {
            return imagesOnly ? IMAGES_MIME_TYPE : null;
        }
        List<String> mimeTypes = new ArrayList<String>();
        List<AcceptedTypeConfig> acceptedTypeConfigs = acceptedTypesConfig.getAcceptedTypeConfigs();
        for (AcceptedTypeConfig acceptedTypeConfig : acceptedTypeConfigs) {
            mimeTypes.add(acceptedTypeConfig.getMimeType());
        }
        if (imagesOnly) {
            mimeTypes.retainAll(IMAGES_MIME_TYPE);
        }
        return mimeTypes;
    }

    private boolean isFileExtensionValid(String fileName) {
        for (String extension : extensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilesExtensionValid(String fileNames) {
        if (extensions == null) {
            return true;
        }
        String[] fileNamesSplit = fileNames.split(",");

        for (String fileName : fileNamesSplit) {
            boolean valid = isFileExtensionValid(fileName);
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    public void setMimeType(Element input) {
        if (mimeTypes == null) {
            return;
        }
        StringBuilder acceptAttributeBuilder = new StringBuilder();
        int size = mimeTypes.size();
        for (String mimeType : mimeTypes) {
            size--;
            acceptAttributeBuilder.append(mimeType);
            if (size != 0) {
                acceptAttributeBuilder.append(", ");
            }
        }
        input.setAttribute("accept", acceptAttributeBuilder.toString());
    }
}
