package ru.intertrust.cm.core.config.localization;

import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 16.02.2015
 *         Time: 11:59
 */
public class MessageKey {
    private String key;
    private String classifier;
    private Map<String, ? extends Object> context;

    public MessageKey(String key) {
        this.key = key;
    }

    public MessageKey(String key, String classifier) {
        this.key = key;
        this.classifier = classifier;
    }

    public MessageKey(String key, String classifier, Map<String, ? extends Object> context) {
        this.key = key;
        this.classifier = classifier;
        this.context = context;
    }

    public String getKey() {
        return key;
    }

    public String getClassifier() {
        return classifier;
    }

    public Map<String, ? extends Object> getContext() {
        return context;
    }
}
