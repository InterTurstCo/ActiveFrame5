package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Root;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import ru.intertrust.cm.core.config.model.TopLevelConfig;
import ru.intertrust.cm.core.model.FatalException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кэш для хранения соответствий тэгов конфигурации верхнего уровня классам, их представляющим
 * @author vmatsukevich
 *         Date: 7/12/13
 *         Time: 7:37 PM
 */
public class TopLevelConfigurationCache {

    private static final String SEARCH_CLASS_PATH = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "**/*.class";

    private static TopLevelConfigurationCache instance = new TopLevelConfigurationCache();
    private Map<String, Class> tagToClassMap = new ConcurrentHashMap<>();

    /**
     * Возврящает экземпляр {@link TopLevelConfigurationCache}
     * @return
     */
    public static TopLevelConfigurationCache getInstance() {
        return instance;
    }

    private TopLevelConfigurationCache() {

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
            processResource(metadataReaderFactory, resource);
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

    private void processResource(MetadataReaderFactory metadataReaderFactory, Resource resource) {
        try {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            if (!metadataReader.getAnnotationMetadata().hasAnnotation(Root.class.getName())) {
                return;
            }

            Class clazz = Class.forName(metadataReader.getClassMetadata().getClassName());
            if (!TopLevelConfig.class.isAssignableFrom(clazz)) {
                return;
            }

            Root rootAnnotation =  (Root) clazz.getAnnotation(Root.class);
            if (rootAnnotation != null && rootAnnotation.name() != null && !rootAnnotation.name().isEmpty()) {
                tagToClassMap.put(rootAnnotation.name(), clazz);
            }
        } catch (IOException e) {
            throw new FatalException("Failed to read metadata of '" + resource + "'", e);
        } catch (ClassNotFoundException e) {
              int i= 0;
//            throw new FatalException("Class '" + resource + "' was found by ResourcePatternResolver but was " +
//                    "not resolved by ClassLoader", e);
        } catch (NoClassDefFoundError e) {
              int i= 0;
        }
    }

}
