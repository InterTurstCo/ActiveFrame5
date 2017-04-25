package ru.intertrust.cm.core.business.api;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class QueryModifierPrompt {
    private Map<String, Integer> idParamsPrompt = new HashMap<String, Integer>();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QueryModifierPrompt) {
            QueryModifierPrompt prompt = (QueryModifierPrompt) obj;
            return idParamsPrompt.equals(prompt.getIdParamsPrompt());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return idParamsPrompt.hashCode();
    }

    @Override
    public String toString() {
        return "idParamsPrompt: " + idParamsPrompt.toString();
    }

    public Map<String, Integer> getIdParamsPrompt() {
        return unmodifiableMap(idParamsPrompt);
    }

    public QueryModifierPrompt appendIdParamsPrompt(String param, int numberOfUniqueIdTypes) {
        idParamsPrompt.put(param, numberOfUniqueIdTypes);
        return this;
    }
}
