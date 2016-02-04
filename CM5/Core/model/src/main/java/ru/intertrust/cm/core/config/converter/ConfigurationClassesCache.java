package ru.intertrust.cm.core.config.converter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.xml.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.ResourcePatternResolver;

import ru.intertrust.cm.core.business.api.MigrationComponent;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.util.PackagesScanner;

/**
 * Кэш для хранения соответствий тэгов конфигурации верхнего уровня классам, их представляющим
 * @author vmatsukevich
 *         Date: 7/12/13
 *         Rewritten by Andrey A.Pirozhkov on Feb 04, 2016
 */
public class ConfigurationClassesCache {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationClassesCache.class);

    private static final String DEFAULT_CLASS_PACKAGE = "ru/intertrust";

    private static ConfigurationClassesCache instance = new ConfigurationClassesCache();
    private Map<String, Class<?>> tagToClassMap = new ConcurrentHashMap<>();
    private Map<String, Class<?>> migrationComponentNameToClassMap = new ConcurrentHashMap<>();

    private List<String> searchClassPackages = Arrays.asList(DEFAULT_CLASS_PACKAGE);

    /**
     * Возвращает экземпляр {@link ConfigurationClassesCache}
     * @return
     */
    public static ConfigurationClassesCache getInstance() {
        return instance;
    }
        
    private ConfigurationClassesCache() {

    }

    /**
     * Конструирует кэш, просматрия все классы на classpath с помощью {@link ResourcePatternResolver} и выбирая те,
     * что реализуют {@link TopLevelConfig} и имеют аннотацию {@link Root} c атрибутом
     * {@link org.simpleframework.xml.Root#name()}
     */
    public void  build() {
        PackagesScanner scanner = new PackagesScanner()
                .addPackages(searchClassPackages)
                .addAnnotationFinder(Root.class, new RootCallback())
                .addAnnotationFinder(MigrationComponent.class, new MigrationComponentCallback());
        scanner.scan();
        if (logger.isInfoEnabled()) {
            PackagesScanner.Statistics stats = scanner.getStatistics();
            logger.info("{} classes scanned in {} ms; {} configuration elements found",
                    stats.getScannedClasses(), stats.getScanTimeMillis(), stats.getFoundClasses());
        }
    }

    /**
     * Возвращает класс, представляющий конфигурацию с именем tagName
     * @param tagName имя конфигурации
     * @return класс, представляющий конфигурацию с именем tagName
     */
    @SuppressWarnings("unchecked")
    public <T> Class<T> getClassByTagName(String tagName) {
        return (Class<T>) tagToClassMap.get(tagName);
    }

    /**
     * Возвращает класс, представляющий миграционный компонент с именем migrationComponentName
     * @param migrationComponentName имя миграционного компонента
     * @return класс, представляющий миграционный компонент
     */
    @SuppressWarnings("unchecked")
    public <T> Class<T> getClassByMigrationComponentName(String migrationComponentName) {
        return (Class<T>) migrationComponentNameToClassMap.get(migrationComponentName);
    }

    private class RootCallback implements PackagesScanner.ClassFoundCallback {

        @Override
        public void processClass(Class<?> foundClass) {
            Root rootAnnotation = foundClass.getAnnotation(Root.class);
            if (rootAnnotation.name() != null && !rootAnnotation.name().isEmpty()){
                tagToClassMap.put(rootAnnotation.name(), foundClass);
            } else {
                String defaultTagName = foundClass.getSimpleName();
                char first = Character.toLowerCase(defaultTagName.charAt(0));
                defaultTagName = first + defaultTagName.substring(1);
                tagToClassMap.put(defaultTagName, foundClass);
            }
        }
    }

    private class MigrationComponentCallback implements PackagesScanner.ClassFoundCallback {

        @Override
        public void processClass(Class<?> foundClass) {
            MigrationComponent migrationComponentAnnotation = foundClass.getAnnotation(MigrationComponent.class);
            if (migrationComponentAnnotation != null) {
                migrationComponentNameToClassMap.put(migrationComponentAnnotation.name(), foundClass);
            }
        }
    }

    public List<String> getSearchClassPackages() {
        return searchClassPackages;
    }

    public void setSearchClassPackages(List<String> searchClassPackages) {
        this.searchClassPackages = searchClassPackages;
    }
}
