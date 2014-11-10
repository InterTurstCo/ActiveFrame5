package ru.intertrust.cm.core.config.importcsv;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

@Root(name = "import-settings")
public class ImportSettingsConfig implements TopLevelConfig {
    private static final long serialVersionUID = 7050302544343250929L;

    @Attribute
    private String name;
    
    @ElementList(required=false, inline=true, entry="before-import")
    private List<BeforeImportConfig> beforeImport;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BeforeImportConfig> getBeforeImport() {
        return beforeImport;
    }

    public void setBeforeImport(List<BeforeImportConfig> beforeImport) {
        this.beforeImport = beforeImport;
    }
}
