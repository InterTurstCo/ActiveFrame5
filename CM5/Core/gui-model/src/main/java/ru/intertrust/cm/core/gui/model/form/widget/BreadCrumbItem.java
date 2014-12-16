package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.12.2014
 *         Time: 21:36
 */
public class BreadCrumbItem {
    private final String name;
    private final String displayText;
    private final CollectionViewerConfig config;

    public BreadCrumbItem(String name, String displayText, CollectionViewerConfig config) {
        this.name = name;
        this.displayText = displayText;
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public CollectionViewerConfig getConfig() {
        return config;
    }
}

