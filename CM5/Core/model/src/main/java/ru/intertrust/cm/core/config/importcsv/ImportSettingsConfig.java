package ru.intertrust.cm.core.config.importcsv;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.List;

@Root(name = "import-settings")
public class ImportSettingsConfig implements TopLevelConfig {
    private static final long serialVersionUID = 7050302544343250929L;

    @Attribute
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;
    
    @ElementList(required=false, inline=true, entry="before-import")
    private List<BeforeImportConfig> beforeImport;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.None;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImportSettingsConfig that = (ImportSettingsConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) return false;
        if (beforeImport != null ? !beforeImport.equals(that.beforeImport) : that.beforeImport != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
