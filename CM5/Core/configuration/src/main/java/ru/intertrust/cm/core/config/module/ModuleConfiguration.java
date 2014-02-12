package ru.intertrust.cm.core.config.module;

import java.net.URL;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name="module")
public class ModuleConfiguration implements Dto {
    
    @Element
    private String name;
    
    @Element
    private String description;

    @ElementList(entry="depend", required=false)
    private List<String> depends;
    
    @ElementList(entry="extension-points-package", required=false, name="extension-points-packages")
    private List<String> extensionPointsPackages;

    @ElementList(entry="gui-components-package", required=false, name="gui-components-packages")
    private List<String> guiComponentsPackages;

    @Element(required=false, name="import-files")
    private ImportFilesConfiguration importFiles;

    @Element(required=false, name="configuration-schema-path")
    private String configurationSchemaPath;
    
    @ElementList(entry="configuration-path", required=false, name="configuration-paths")
    private List<String> configurationPaths;

    private URL moduleUrl;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getConfigurationSchemaPath() {
        return configurationSchemaPath;
    }
    public void setConfigurationSchemaPath(String configurationSchemaPath) {
        this.configurationSchemaPath = configurationSchemaPath;
    }
    public List<String> getConfigurationPaths() {
        return configurationPaths;
    }
    public void setConfigurationPaths(List<String> configurationPaths) {
        this.configurationPaths = configurationPaths;
    }
    public List<String> getDepends() {
        return depends;
    }
    public void setDepends(List<String> depends) {
        this.depends = depends;
    }
    public List<String> getExtensionPointsPackages() {
        return extensionPointsPackages;
    }
    public void setExtensionPointsPackages(List<String> extensionPointsPackages) {
        this.extensionPointsPackages = extensionPointsPackages;
    }
    public List<String> getGuiComponentsPackages() {
        return guiComponentsPackages;
    }
    public void setGuiComponentsPackages(List<String> guiComponentsPackages) {
        this.guiComponentsPackages = guiComponentsPackages;
    }
    public ImportFilesConfiguration getImportFiles() {
        return importFiles;
    }
    public void setImportFiles(ImportFilesConfiguration importFiles) {
        this.importFiles = importFiles;
    }
    public URL getModuleUrl() {
        return moduleUrl;
    }
    public void setModuleUrl(URL moduleUrl) {
        this.moduleUrl = moduleUrl;
    }
    
}
