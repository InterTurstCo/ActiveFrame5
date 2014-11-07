package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 30.10.14.
 */

@Root(name = "domain-object-types")
public class PatternDomainObjectTypesConfig implements Dto {

    @ElementList(name = "domain-object-type", entry = "domain-object-type", inline = true, required = false)
    private List<PatternDomainObjectTypeConfig> patternDomainObjectTypeConfig = new ArrayList<>();

    public List<PatternDomainObjectTypeConfig> getPatternDomainObjectTypeConfig() {
        return patternDomainObjectTypeConfig;
    }

    public void setPatternDomainObjectTypeConfig(List<PatternDomainObjectTypeConfig> patternDomainObjectTypeConfig) {
        this.patternDomainObjectTypeConfig = patternDomainObjectTypeConfig;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternDomainObjectTypesConfig that = (PatternDomainObjectTypesConfig) o;

        if (patternDomainObjectTypeConfig != null ? !patternDomainObjectTypeConfig.equals(that.patternDomainObjectTypeConfig) : that.patternDomainObjectTypeConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return patternDomainObjectTypeConfig != null ? patternDomainObjectTypeConfig.hashCode() : 0;
    }
}
