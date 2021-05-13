package ru.intertrust.cm.core.business.api.extensions;

import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;

import java.io.File;

public interface AfterUploadReportPackageExtension extends ExtensionPointHandler {
    void execute(File reportPackage);
}
