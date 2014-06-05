package ru.intertrust.cm.core.gui.impl.client.attachment;

import com.google.gwt.dom.client.Element;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class ExtensionValidator {
    private AcceptedTypesConfig acceptedTypesConfig;
    private List<String> extensions;

    public ExtensionValidator(AcceptedTypesConfig acceptedTypesConfig) {
        this.acceptedTypesConfig = acceptedTypesConfig;
        extensions = initExtensions();
    }

    private List<String> initExtensions() {
        if (acceptedTypesConfig == null) {
            return null;
        }
        List<String> extensions = new ArrayList<String>();
        List<AcceptedTypeConfig> acceptedTypeConfigs = acceptedTypesConfig.getAcceptedTypeConfigs();
        for (AcceptedTypeConfig acceptedTypeConfig : acceptedTypeConfigs) {
            List<String> extensionsForMimeType = getExtensionsForMimeType(acceptedTypeConfig);
            extensions.addAll(extensionsForMimeType);
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

    private boolean isFileExtensionValid(String fileName) {

        for (String extension : extensions) {
            if (fileName.contains(extension)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilesExtensionValid(String fileNames) {
        if (extensions == null) {
            return true;
        }
        String[] fileNamesSplitted = fileNames.split(",");

        for (String fileName : fileNamesSplitted) {
            boolean valid = isFileExtensionValid(fileName);
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    public void setMimeType(Element input) {
        if (acceptedTypesConfig == null) {
            return;
        }
        StringBuilder acceptAttributeBuilder = new StringBuilder();
        List<AcceptedTypeConfig> acceptedTypeConfigs = acceptedTypesConfig.getAcceptedTypeConfigs();
        int size = acceptedTypeConfigs.size();
        for (AcceptedTypeConfig acceptedTypeConfig : acceptedTypeConfigs) {
            size--;
            acceptAttributeBuilder.append(acceptedTypeConfig.getMimeType());
            if (size != 0) {
                acceptAttributeBuilder.append(", ");
            }
        }
        input.setAttribute("accept", acceptAttributeBuilder.toString());
    }
}
