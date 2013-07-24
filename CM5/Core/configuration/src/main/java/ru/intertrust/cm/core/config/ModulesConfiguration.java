package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Представляет конфигурацию конфигураций модулей
 * @author vmatsukevich
 *         Date: 7/9/13
 *         Time: 5:45 PM
 */
@Root
public class ModulesConfiguration implements Serializable {

    @ElementList(entry = "module-configuration", inline = true)
    private List<ModuleConfig> moduleConfigs = new ArrayList<>();

    public List<ModuleConfig> getModuleConfigs() {
        return moduleConfigs;
    }

    public void setModuleConfigs(List<ModuleConfig> moduleConfigs) {
        this.moduleConfigs = moduleConfigs;
    }

}
