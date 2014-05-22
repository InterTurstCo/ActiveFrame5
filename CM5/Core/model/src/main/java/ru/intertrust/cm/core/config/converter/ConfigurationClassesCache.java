package ru.intertrust.cm.core.config.converter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.simpleframework.xml.Root;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Кэш для хранения соответствий тэгов конфигурации верхнего уровня классам, их представляющим
 * @author vmatsukevich
 *         Date: 7/12/13
 *         Time: 7:37 PM
 */
public class ConfigurationClassesCache {

    private static final String SEARCH_CLASS_PATH = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "**/*.class";

    private static ConfigurationClassesCache instance = new ConfigurationClassesCache();
    private Map<String, Class> tagToClassMap = new ConcurrentHashMap<>();

    /**
     * Возврящает экземпляр {@link ConfigurationClassesCache}
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
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        Resource[] resources = readResources(resourcePatternResolver);

        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            processResource(metadataReaderFactory, resource, resourcePatternResolver.getClassLoader());
        }
    }

    /**
     * Возвращает класс, представляющий конфигурацию с именем tagName
     * @param tagName имя конфигурации
     * @return класс, представляющий конфигурацию с именем tagName
     */
    public Class getClassByTagName(String tagName) {
        return tagToClassMap.get(tagName);
    }

    private Resource[] readResources(ResourcePatternResolver resourcePatternResolver) {
        try {
            return resourcePatternResolver.getResources(SEARCH_CLASS_PATH);
        } catch (IOException e) {
            throw new FatalException("ResourcePatternResolver failed to find resources on classpath", e);
        }
    }

    private void processResource(MetadataReaderFactory metadataReaderFactory, Resource resource,
                                 ClassLoader classLoader) {
        try {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            if (!metadataReader.getAnnotationMetadata().hasAnnotation(Root.class.getName())) {
                return;
            }

            Class clazz = classLoader.loadClass(metadataReader.getClassMetadata().getClassName());
            Root rootAnnotation =  (Root) clazz.getAnnotation(Root.class);
            if (rootAnnotation != null) {
                if (rootAnnotation.name() != null && !rootAnnotation.name().isEmpty()){
                    tagToClassMap.put(rootAnnotation.name(), clazz);
                }else{
                    String defaultTagName = clazz.getSimpleName();
                    char first = Character.toLowerCase(defaultTagName.charAt(0));
                    defaultTagName = first + defaultTagName.substring(1);
                    tagToClassMap.put(defaultTagName, clazz);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new FatalException("Class '" + resource + "' was found by ResourcePatternResolver but was " +
                    "not resolved by ClassLoader", e);
        } catch (Throwable e) {
            throw new FatalException("Failed to read metadata of '" + resource + "'", e);
        }
    }

}
