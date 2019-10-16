package ru.intertrust.cm.core.config.module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.intertrust.cm.core.config.ConfigurationSchemaValidator;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Сервис управления модулями. Сервис при старте просматривает все файлы
 * cm.module.xml и создает реестр модулей в системе
 * @author larin
 * 
 */
public class ModuleService {

    private static final String MODULE_XSD = "config/module.xsd";
    private ModuleGraf fileGraf;
    private List<ModuleConfiguration> moduleList = new ArrayList<ModuleConfiguration>();

    public void init() {
        URL url = null;
        try {
            fileGraf = new ModuleGraf();
            // Получение ресурсов описания модулей и добавление их в граф для
            // сортировки с учетом зависимостей
            Enumeration<URL> urlEnum =
                    this.getClass().getClassLoader().getResources("META-INF/cm-module.xml");
            while (urlEnum.hasMoreElements()) {
                url = urlEnum.nextElement();
                validateSchema(url); // Проверка на соответствие XML Schema в
                                     // module.xsd
                ModuleConfiguration config = loadModule(url);
                config.setModuleUrl(new URL(url.toString().substring(0, url.toString().indexOf("META-INF"))));
                fileGraf.addModuleConfiguration(config, config.getDepends());
            }

            // Проверка на зацикливание
            fileGraf.check();

            // Обход графа модулей с учетом зависимостей
            while (fileGraf.hasMoreElements()) {
                ModuleNode moduleNode = fileGraf.nextElement();
                moduleList.add(moduleNode.getModuleConfiguration());
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
        private List<String> dependOn = new ArrayList<String>();
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
    public class ModuleGraf implements Enumeration<ModuleNode> {
        private Hashtable<String, ModuleNode> importFilesGraf = new Hashtable<String, ModuleNode>();

        public void addModuleConfiguration(ModuleConfiguration moduleConfiguration, String[] dependsOn) {
            ModuleNode node = new ModuleNode();
            node.name = moduleConfiguration.getName();
            node.setModuleConfiguration(moduleConfiguration);
            if (dependsOn != null) {
                for (String dependOn : dependsOn) {
                    node.dependOn.add(dependOn);
                }
            }
            importFilesGraf.put(node.name, node);

        }

        public void addModuleConfiguration(ModuleConfiguration moduleConfiguration, List<String> dependsOn) {
            String[] depends = null;
            if (dependsOn != null) {
                depends = dependsOn.toArray(new String[dependsOn.size()]);
            }
            addModuleConfiguration(moduleConfiguration, depends);
        }

        /**
         * Проверка на зацикливание
         */
        public void check() {
            // Цикл по элементам графа
            for (ModuleNode node : importFilesGraf.values()) {
                // Проверка каждого элемента графа
                check(node, new Stack<String>());
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
                    throw new FatalException("Check dependenses import data files fail. Found cyclic on file "
                            + stack.toString());
                } else {
                    // Более понятная ошибка, если модуль не найден
                    if (importFilesGraf.get(dependOn) == null) {
                        throw new FatalException("Not found module " + dependOn);
                    }
                    check(importFilesGraf.get(dependOn), stack);
                }
            }
            stack.pop();
        }

        @Override
        public boolean hasMoreElements() {
            // Поиск хотя бы одного не импортированного
            for (ModuleNode node : importFilesGraf.values()) {
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
            for (ModuleNode node : importFilesGraf.values()) {
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
                    result = getDependOn(importFilesGraf.get(dependOn));
                    if (result == null) {
                        if (!importFilesGraf.get(dependOn).isLoad) {
                            result = importFilesGraf.get(dependOn);
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
    }
}
