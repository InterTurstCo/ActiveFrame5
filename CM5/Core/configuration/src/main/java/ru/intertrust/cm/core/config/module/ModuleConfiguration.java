package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.net.URL;
import java.util.List;

@Root(name="module")
public class ModuleConfiguration implements Dto {
    private static final long serialVersionUID = 5237710895366672078L;

    @Element
    private String name;
    
    @Element
    private String description;

    @ElementList(entry="depend", required=false)
    private List<String> depends;
    
    @ElementList(entry="extension-points-package", required=false, name="extension-points-packages")
    private List<String> extensionPointsPackages;

    @ElementList(entry="configuration-elements-package", required=false, name="configuration-elements-packages")
    private List<String> configurationElementsPackages;

    @ElementList(entry="gui-components-package", required=false, name="gui-components-packages")
    private List<String> guiComponentsPackages;

    @ElementList(entry="server-components-package", required=false, name="server-components-packages")
    private List<String> serverComponentsPackages;

    @ElementList(required=false, entry="import-files", inline=true)
    private List<ImportFilesConfiguration> importFiles;

    @Element(required=false, name="configuration-schema-path")
    private String configurationSchemaPath;

    @ElementList(entry="configuration-path", required=false, name="configuration-paths")
    private List<String> configurationPaths;

    @Element(required=false, name="import-reports")
    private ImportReportsConfiguration importReports;

    @ElementList(required=false, name="deploy-processes", entry="process-definition")
    private List<String> deployProcesses;

    @Element(name="localization-files", required = false)
    private LocalizationFilesConfiguration localisationFiles;

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
    public List<String> getConfigurationElementsPackages() {
        return configurationElementsPackages;
    }
    public void setConfigurationElementsPackages(List<String> configurationElementsPackages) {
        this.configurationElementsPackages = configurationElementsPackages;
    }
    public List<String> getGuiComponentsPackages() {
        return guiComponentsPackages;
    }
    public void setGuiComponentsPackages(List<String> guiComponentsPackages) {
        this.guiComponentsPackages = guiComponentsPackages;
    }        
    public List<String> getServerComponentsPackages() {
        return serverComponentsPackages;
    }
    public void setServerComponentsPackages(List<String> serverComponentsPackages) {
        this.serverComponentsPackages = serverComponentsPackages;
    }
    public List<ImportFilesConfiguration> getImportFiles() {
        return importFiles;
    }
    public void setImportFiles(List<ImportFilesConfiguration> importFiles) {
        this.importFiles = importFiles;
    }

    public ImportReportsConfiguration getImportReports() {
        return importReports;
    }

    public void setImportReports(ImportReportsConfiguration importReports) {
        this.importReports = importReports;
    }

    public URL getModuleUrl() {
        return moduleUrl;
    }
    public void setModuleUrl(URL moduleUrl) {
        this.moduleUrl = moduleUrl;
    }
    public List<String> getDeployProcesses() {
        return deployProcesses;
    }
    public void setDeployProcesses(List<String> deployProcesses) {
        this.deployProcesses = deployProcesses;
    }

    public LocalizationFilesConfiguration getLocalisationFiles() {
        return localisationFiles;
    }
}
