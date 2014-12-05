package ru.intertrust.cm.core.business.load;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.Localizer;
import ru.intertrust.cm.core.config.module.LocalizationFileConfiguration;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * @author Lesia Puhova
 *         Date: 03.12.14
 *         Time: 18:05
 */


public class LocalizerImpl implements Localizer, Localizer.Remote {

    @Autowired
    private ModuleService moduleService;

    private Map properties = new Properties();

    @Override
    public void load() {
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getLocalisationFiles() != null) {
                URL moduleUrl = moduleConfiguration.getModuleUrl();
                for (LocalizationFileConfiguration config : moduleConfiguration.getLocalisationFiles().getLocalizationFiles()) {
                    try {
                        URL fileUrl = new URL(moduleUrl.toString() + config.getFilePath());
                        Properties moduleProps = new Properties();
                        InputStream inputStream = fileUrl.openStream();
                        Reader reader = new InputStreamReader(inputStream, "UTF-8");
                        moduleProps.load(reader);
                        properties.putAll(moduleProps);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public String getDisplayText(String value, String classifier, Map<String, ? extends Object> context) {
        String displayText = (String)properties.get(createKey(value, classifier, context));
        return displayText != null ? displayText : value;
    }

    @Override
    public String getDisplayText(String value, String classifier) {
        return getDisplayText(value, classifier, null);
    }


    private static String createKey( String value, String classifier, Map<String, ? extends Object> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(classifier.toUpperCase()).append("]");
        //TODO: add extra contexts here, if any.
        if (context != null && (FIELD.equals(classifier) || SEARCH_FIELD.equals(classifier) )) {
            sb.append(context.get(DOMAIN_OBJECT_CONTEXT)).append(".");
        }
        sb.append(value);
        return sb.toString();
    }
}
