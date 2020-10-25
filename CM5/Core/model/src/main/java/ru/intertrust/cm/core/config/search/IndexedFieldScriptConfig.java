package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

public class IndexedFieldScriptConfig implements Serializable {

    public enum ScriptReturnType {
        STRING("string"),
        DATE("date"),
        BOOLEAN("boolean"),
        LONG("long"),
        DECIMAL("decimal");

        public final String xmlValue;

        private ScriptReturnType(String xmlValue) {
            this.xmlValue = xmlValue;
        }

        static ScriptReturnType fromXmlValue(String xmlValue) {
            for (ScriptReturnType value : values()) {
                if (value.xmlValue.equalsIgnoreCase(xmlValue)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown return-type value: " + xmlValue);
        }
    }

    private ScriptReturnType scriptReturnType;

    @Element(required = false)
    private String script;

    public String getScript() {
        return script;
    }

    @Attribute(name = "return-type", required = false)
    public String getScriptReturnTypeString() {
        return scriptReturnType == null ? null : scriptReturnType.xmlValue;
    }

    public ScriptReturnType getScriptReturnType() {
        return scriptReturnType == null ? ScriptReturnType.STRING : scriptReturnType;
    }

    @Attribute(name = "return-type", required = false)
    public void setScriptReturnTypeString(String scriptReturnType) {
        this.scriptReturnType = ScriptReturnType.fromXmlValue(scriptReturnType);
    }

    @Override
    public int hashCode() {
        int hash = script != null ? script.hashCode() : 0;
        hash = hash * 31 ^ (scriptReturnType != null ? scriptReturnType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        IndexedFieldScriptConfig other = (IndexedFieldScriptConfig) obj;
        return (script == null ? other.script == null : script.equals(other.script))
                && (scriptReturnType == null ? other.scriptReturnType == null : scriptReturnType.equals(other.scriptReturnType));
    }
}
