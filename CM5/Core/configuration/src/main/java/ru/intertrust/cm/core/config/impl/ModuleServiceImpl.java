package ru.intertrust.cm.core.config.impl;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.config.ConfigurationSchemaValidator;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.model.FatalException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class ModuleServiceImpl implements ModuleService {
    private static final String MODULE_XSD = "config/module.xsd";
    private ModuleGraph fileGraph;
    private final List<ModuleConfiguration> moduleList = new ArrayList<>();
    private final List<ModuleConfiguration> rootModuleList = new ArrayList<>();

    public void init() {
        URL url = null;
        try {
            fileGraph = new ModuleGraph();
            // Получение ресурсов описания модулей и добавление их в граф для
            // сортировки с учетом зависимостей
            Enumeration<URL> urlEnum =
                    this.getClass().getClassLoader().getResources("META-INF/cm-module.xml");
            while (urlEnum.hasMoreElements()) {
                url = urlEnum.nextElement();
                validateSchema(url); // Проверка на соответствие XML Schema в module.xsd
                ModuleConfiguration config = loadModule(url);
                config.setModuleUrl(new URL(url.toString().substring(0, url.toString().indexOf("META-INF"))));
                fileGraph.addModuleConfiguration(config, config.getDepends());
            }

            // Проверка на зацикливание
            fileGraph.check();

            // Обход графа модулей с учетом зависимостей
            while (fileGraph.hasMoreElements()) {
                ModuleNode moduleNode = fileGraph.nextElement();
                moduleList.add(moduleNode.getModuleConfiguration());
                if (moduleNode.dependOn.size() == 0){
                    rootModuleList.add(moduleNode.getModuleConfiguration());
                }
            }

        } catch (Exception ex) {
            throw new FatalException("Cannot load module: " + (url != null ? url.toString() : "null"), ex);
        }
    }

    private void validateSchema(URL url) throws IOException {
        try (InputStream stream = url.openStream()) {
            ConfigurationSchemaValidator validator = new ConfigurationSchemaValidator(stream, MODULE_XSD);
            validator.validate();
        }

    }

    public List<ModuleConfiguration> getModuleList() {
        return moduleList;
    }

    @Override
    public List<ModuleConfiguration> getRootModules() {
        return rootModuleList;
    }

    @Override
    public List<ModuleConfiguration> getChildModules(String moduleName) {
        List<ModuleConfiguration> result = new ArrayList<>();
        for (ModuleNode node : fileGraph.getModuleNodes()) {
            if (node.getDependOn().contains(moduleName)) {
                result.add(node.getModuleConfiguration());
            }
        }
        return result;
    }

    @Override
    public List<ModuleConfiguration> getParentModules(String moduleName) {
        List<ModuleConfiguration> result = new ArrayList<>();
        ModuleNode moduleNode = fileGraph.getModuleNode(moduleName);
        for (String parent : moduleNode.getDependOn()) {
            result.add(fileGraph.getModuleNode(parent).getModuleConfiguration());
        }
        return result;
    }

    /**
     * Загрузка одного модуля
     * @param moduleConfigUrl
     * @return
     */
    private ModuleConfiguration loadModule(URL moduleConfigUrl) {
        InputStream source = null;
        try {
            Serializer serializer = new Persister();
            source = moduleConfigUrl.openStream();
            ModuleConfiguration config = serializer.read(ModuleConfiguration.class, source);
            return config;
        } catch (Exception ex) {
            throw new FatalException("Can not load cm module " + moduleConfigUrl, ex);
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
            } catch (Exception ignoreEx) {
            }
        }
    }

    public class ModuleNode {
        private String name;
        private final List<String> dependOn = new ArrayList<>();
        private boolean isLoad;
        private ModuleConfiguration moduleConfiguration;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getDependOn() {
            return dependOn;
        }

        public ModuleConfiguration getModuleConfiguration() {
            return moduleConfiguration;
        }

        public void setModuleConfiguration(ModuleConfiguration moduleConfiguration) {
            this.moduleConfiguration = moduleConfiguration;
        }
    }

    /**
     * Класс реализации графа импортируемых файлов с учетом зависимостей
     * @author larin
     *
     */
    public class ModuleGraph implements Enumeration<ModuleNode> {
        private final Hashtable<String, ModuleNode> importFilesGraph = new Hashtable<String, ModuleNode>();

        public void addModuleConfiguration(ModuleConfiguration moduleConfiguration, String[] dependsOn) {
            ModuleNode node = new ModuleNode();
            node.name = moduleConfiguration.getName();
            node.setModuleConfiguration(moduleConfiguration);
            if (dependsOn != null) {
                Collections.addAll(node.dependOn, dependsOn);
            }
            importFilesGraph.put(node.name, node);

        }

        public void addModuleConfiguration(ModuleConfiguration moduleConfiguration, List<String> dependsOn) {
            String[] depends = null;
            if (dependsOn != null) {
                depends = dependsOn.toArray(new String[0]);
            }
            addModuleConfiguration(moduleConfiguration, depends);
        }

        /**
         * Проверка на зацикливание
         */
        public void check() {
            // Цикл по элементам графа
            for (ModuleNode node : importFilesGraph.values()) {
                // Проверка каждого элемента графа
                check(node, new Stack<>());
            }
        }

        /**
         * Рекурсивная функция проверки зависимости импортируемых файлов на
         * зацикливание зависимостей
         * @param node
         * @param stack
         */
        private void check(ModuleNode node, Stack<String> stack) {
            stack.push(node.name);
            for (String dependOn : node.dependOn) {
                if (stack.contains(dependOn)) {
                    throw new FatalException("Check dependencies import data files fail. Found cyclic on file "
                            + stack.toString());
                } else {
                    // Более понятная ошибка, если модуль не найден
                    if (importFilesGraph.get(dependOn) == null) {
                        throw new FatalException("Not found module " + dependOn);
                    }
                    check(importFilesGraph.get(dependOn), stack);
                }
            }
            stack.pop();
        }

        @Override
        public boolean hasMoreElements() {
            // Поиск хотя бы одного не импортированного
            for (ModuleNode node : importFilesGraph.values()) {
                if (!node.isLoad) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public ModuleNode nextElement() {
            ModuleNode result = null;
            // Поиск первого не импортированного
            for (ModuleNode node : importFilesGraph.values()) {
                if (!node.isLoad) {
                    // Нашли не импортированного, получаем по цепочке
                    // зависимостей ближайший не импортированный
                    result = getDependOn(node);
                    break;
                }
            }
            if (result != null) {
                result.isLoad = true;
            }
            return result;
        }

        /**
         * Поднимаемся по ветке зависимых файлов и ищем наиболее верхние не
         * загруженные
         * @param node
         * @return
         */
        private ModuleNode getDependOn(ModuleNode node) {
            ModuleNode result = null;
            // Элемент верхнего уровня, который не от кого не зависит
            if (node.dependOn.size() == 0) {
                // Проверяем его на загруженность
                if (!node.isLoad) {
                    result = node;
                }
            } else {
                // Элемент не верхнего уровня, проверяем загруженность элементов
                // верхнего уровня
                for (String dependOn : node.dependOn) {
                    result = getDependOn(importFilesGraph.get(dependOn));
                    if (result == null) {
                        if (!importFilesGraph.get(dependOn).isLoad) {
                            result = importFilesGraph.get(dependOn);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }

            if (result == null && !node.isLoad) {
                result = node;
            }

            return result;
        }

        /**
         * Получение модуля по имени
         * @param moduleName
         * @return
         */
        public ModuleNode getModuleNode(String moduleName){
            return importFilesGraph.get(moduleName);
        }

        /**
         * Возвращает все модули без сортировки, без учета зависимотей друг от друга
         * @return
         */
        public Collection<ModuleNode> getModuleNodes() {
            return importFilesGraph.values();
        }

    }

}
