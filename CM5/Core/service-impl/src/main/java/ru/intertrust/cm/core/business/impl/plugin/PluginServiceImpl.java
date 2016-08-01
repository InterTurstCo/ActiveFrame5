package ru.intertrust.cm.core.business.impl.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.FatalException;

public class PluginServiceImpl implements PluginService {
    private static final String PLUGIN_FOLDER_PROPERTY = "${plugin.folder}";
    private Map<String, PluginInfo> pluginsInfo;
    private Map<String, ApplicationContext> contexts = new HashMap<String, ApplicationContext>();
    private PluginClassLoader classLoader;

    private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);

    @Autowired
    private PropertyResolver propertyResolver;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        //Очищаем временные каталоги с плагинами
        String pluginFolderPath = getPluginFolder();
        if (pluginFolderPath != null) {
            File pluginFolder = new File(pluginFolderPath);
            File[] subfolders = pluginFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File folderFile) {
                    return folderFile.isDirectory();
                }
            });

            for (File subfolder : subfolders) {
                try {
                    FileUtils.deleteDirectory(subfolder);
                } catch (Exception ex) {
                    //Пишем в лог, невозможность очистить временный каталог не должен мешать запуску сервера
                    logger.warn("Error delete temp folder", ex);
                }
            }
        }
    }

    @Override
    public void init(String contextName, ApplicationContext applicationContext) {
        contexts.put(contextName, applicationContext);
        //Автостарт плагина
        for (PluginInfo pluginInfo : getPlugins().values()) {
            if (pluginInfo.isAutostart() && pluginInfo.getContextName().equals(contextName)) {
                String result = executePluginInternal(pluginInfo.getClassName(), null, false);
                logger.info("Autostart plugin " + pluginInfo.getClassName() + ". Result: " + result);
            }
        }
    }

    @Override
    public Map<String, PluginInfo> getPlugins() {
        try {
            synchronized (PluginServiceImpl.class) {
                if (pluginsInfo == null) {
                    initPluginsInfo();
                }
                return pluginsInfo;
            }
        } catch (Exception ex) {
            throw new FatalException("Error get plugin list", ex);
        }
    }

    private void initPluginsInfo() throws ClassNotFoundException, IOException {
        String pluginFolder = getPluginFolder();
        URL[] urls = new URL[0];

        if (pluginFolder != null) {
            //Создаем временную папку для джаров, из за проблемы освобождения ресурсов при передеплое и копируем туда все джары
            File tempFolder = new File(pluginFolder, String.valueOf(System.currentTimeMillis()));
            tempFolder.mkdirs();

            //Ищем все jar файлы
            File folder = new File(pluginFolder);
            File[] jars = folder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }

            });

            urls = new URL[jars.length];
            //Копируем во временную директорию и формируем массив URL
            for (int i = 0; i < jars.length; i++) {
                FileUtils.copyFileToDirectory(jars[i], tempFolder);
                File tempJar = new File(tempFolder, jars[i].getName());
                urls[i] = tempJar.toURI().toURL();
            }
        }

        classLoader = new PluginClassLoader(urls, this.getClass().getClassLoader());

        pluginsInfo = new HashMap<String, PluginInfo>();
        for (String basePackage : getUniqueBasePackages()) {

            // Сканирование класспаса
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Plugin.class));
            scanner.setResourceLoader(new DefaultResourceLoader(classLoader));

            // Цикл по найденным классам
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                String className = bd.getBeanClassName();
                // Получение найденного класса
                Class<?> pluginClass = classLoader.loadClass(className);
                // Получение анотации Plugin
                Plugin annatation = (Plugin) pluginClass.getAnnotation(Plugin.class);
                if (annatation != null) {
                    pluginsInfo.put(className,
                            new PluginInfo(className, annatation.name(), annatation.description(), annatation.context(), annatation.autostart()));
                }
            }
        }

    }

    @Override
    public String executePlugin(String id, String param) {
        return executePluginInternal(id, param, true);
    }

    private String executePluginInternal(String id, String param, boolean checkPermissions) {
        if (checkPermissions && !checkPermissions()) {
            throw new FatalException("Current user not permit execute plugin");
        }

        try {
            PluginInfo pluginInfo = pluginsInfo.get(id);
            ApplicationContext context = contexts.get(pluginInfo.getContextName());
            PluginHandler pluginHandler =
                    (PluginHandler) context.getAutowireCapableBeanFactory().createBean(
                            classLoader.loadClass(pluginInfo.getClassName()),
                            AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                            true);
            return pluginHandler.execute(param);
        } catch (Exception ex) {
            throw new FatalException("Error execute plugin.", ex);
        }
    }

    @Override
    public void deployPluginPackage(String filePath) {

        if (!checkPermissions()) {
            throw new FatalException("Current user not permit deploy plugin");
        }

        String pluginFolder = getPluginFolder();
        if (pluginFolder == null) {
            throw new FatalException("Error deploy plugin package. Property " + PLUGIN_FOLDER_PROPERTY + " not set.");
        }

        try {
            synchronized (PluginServiceImpl.class) {
                pluginsInfo = null;
                classLoader.close();
                classLoader = null;
            }

            File newPackage = new File(pluginFolder, getFileName(filePath));
            if (newPackage.exists()) {
                if (!newPackage.delete()) {
                    throw new FatalException("Error redeploy plugin.");
                }
            }
            FileUtils.copyFile(new File(filePath), newPackage);

        } catch (FatalException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FatalException("Error deploy plugin package.", ex);
        } finally {
            getPlugins();
        }
    }

    private String getFileName(String filePath) throws IOException {
        File file = new File(filePath);
        JarFile jar = null;
        try {
            jar = new JarFile(file);
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String title = attributes.getValue("Implementation-Title");
            title = title.replaceAll(" ", "-") + ".jar";
            return title;
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
    }

    private String getPluginFolder() {
        String result = null;
        if (!propertyResolver.resolvePlaceholders(PLUGIN_FOLDER_PROPERTY).equals(PLUGIN_FOLDER_PROPERTY)) {
            result = propertyResolver.resolvePlaceholders(PLUGIN_FOLDER_PROPERTY);
        }
        return result;
    }

    /**
     * Получение не повторяющегося списка пакетов
     * @return
     */
    private List<String> getUniqueBasePackages() {

        List<String> result = new ArrayList<String>();
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getExtensionPointsPackages() != null) {
                for (String extensionPointsPackage : moduleConfiguration.getExtensionPointsPackages()) {
                    if (!result.contains(extensionPointsPackage)) {
                        result.add(extensionPointsPackage);
                    }
                }
            }
        }
        return result;
    }

    private boolean checkPermissions() {
        UserGroupGlobalCache userCache = context.getBean(UserGroupGlobalCache.class);
        CurrentUserAccessor currentUserAccessor = context.getBean(CurrentUserAccessor.class);
        return userCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId());
    }
}
