package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 17:15
 */
@Root(name = "business-universe")
public class BusinessUniverseConfig implements TopLevelConfig {
    @Element(name = "logo", required = false)
    private LogoConfig logoConfig;

    public LogoConfig getLogoConfig() {
        return logoConfig;
    }

    public void setLogoConfig(LogoConfig logoConfig) {
        this.logoConfig = logoConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BusinessUniverseConfig that = (BusinessUniverseConfig) o;

        if (logoConfig != null ? !logoConfig.equals(that.logoConfig) : that.logoConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return logoConfig != null ? logoConfig.hashCode() : 0;
    }

    @Override
    public String getName() {
        return "business_universe";
    }
}
