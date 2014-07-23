package ru.intertrust.cm.core.config.gui.navigation;

import java.util.HashMap;
import java.util.Map;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root
public abstract class PluginConfig implements Dto {
    private Map<String, Object> historyData = new HashMap<>();

    public abstract String getComponentName();

    public void addHistoryValue(final String key, final Object value) {
        historyData.put(key, value);
    }

    public <T> T getHistoryValue(final String key) {
        return historyData == null ? null : (T) historyData.get(key);
    }

    public <T>  void addHistoryValues(final Map<String, T> values) {
        historyData.putAll(values);
    }

    public Map<String, Object> getHistoryValues() {
        return historyData;
    }
}
