package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class SearchLanguageConfig implements Dto {

    @Attribute(name = "id", required = true)
    private String langId;

    @Attribute(name = "weight", required = false)
    private Long weight;

    public String getLangId() {
        return langId;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        int result = langId.hashCode();
        result = result * 31 + (weight != null ? weight.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        SearchLanguageConfig that = (SearchLanguageConfig) obj;
        if (!langId.equals(that.langId)) {
            return false;
        }
        if (weight != null ? !weight.equals(that.weight) : that.weight != null) {
            return false;
        }
        return true;
    }
}
