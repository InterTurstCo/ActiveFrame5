package ru.intertrust.cm.deployment.tool.config;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
public final class AppConstants {

    private AppConstants() {
    }

    public static final String ARCHIVE_NAME_PATTERN = "%s-%s.zip";
    public static final String SERVER_URL_PATTERN = "http://%s:%s";
    public static final String APP_URL_PATTERN = "http://%s:%s/%s";
    public static final String INIT_DATA_PROPERTY_NAME = "REPOSITORY_INIT_FOLDER";
}
