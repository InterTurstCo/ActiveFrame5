package ru.intertrust.cm.core.business.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ImportDataConfig;
import ru.intertrust.cm.core.config.ImportFileConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Точка расширения запускается после загрузки конфигурации. Данная точка расширения загружает системные справочники из
 * CSV файлов. Все CSV файлы ищутся по пути ru.intertrust.cm.core.importdata, найденные файлы сортируются по
 * наименованию.
 * @author larin
 * 
 */
public class ImportSystemData {

    private static final Logger logger = Logger.getLogger(ImportSystemData.class);
    
    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AccessControlService accessControlService;

    /*
     * Граф импортируемых файлов
     */
    private FileGraf fileGraf;

    public void onLoad() {
        try {
            fileGraf = new FileGraf();

            //Получение ресурсов 
            Enumeration<URL> urlEnum =
                    this.getClass().getClassLoader().getResources("ru/intertrust/cm/core/importdata/import.xml");

            //Строим граф зависимостей импортируемых файлов
            while (urlEnum.hasMoreElements()) {
                URL url = (URL) urlEnum.nextElement();
                Serializer serializer = new Persister();
                InputStream source = url.openStream();
                ImportDataConfig config = serializer.read(ImportDataConfig.class, source);
                source.close();
                for (ImportFileConfig importFileConfig : config.getFile()) {
                    FileNode fileNode = new FileNode();
                    fileNode.name = importFileConfig.getName();
                    fileGraf.importFilesGraf.put(fileNode.name, fileNode);
                    if (importFileConfig.getDepend() != null) {
                        for (String dependOn : importFileConfig.getDepend()) {
                            fileNode.dependOn.add(dependOn);
                        }
                    }
                }

            }

            //Проверка на зацикливание
            fileGraf.check();

            //Обход графа с учетом зависимостей
            while (fileGraf.hasMoreElements()) {
                FileNode fileNode = fileGraf.nextElement();
                ImportData ImportData =
                        new ImportData(collectionsDao, configurationExplorer, domainObjectDao, accessControlService,
                                null);

                URL fileUrl =
                        this.getClass().getClassLoader()
                                .getResource("ru/intertrust/cm/core/importdata/" + fileNode.name);
                logger.info("Import system data from file \"ru/intertrust/cm/core/importdata/" + fileNode.name + "\"");
                ImportData.importData(readFile(fileUrl));
            }

        } catch (Exception ex) {
            throw new FatalException("Can not load system dictionaries.", ex);
        }
    }

    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] readFile(URL fileUrl) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = fileUrl.openStream();
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }

    public class FileNode {
        private String name;
        private List<String> dependOn = new ArrayList<String>();
        private boolean isLoad;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getDependOn() {
            return dependOn;
        }

        public void setDependOn(List<String> dependOn) {
            this.dependOn = dependOn;
        }
    }

    /**
     * Класс реализации графа импортируемых файлов с учетом зависимостей
     * @author larin
     * 
     */
    public class FileGraf implements Enumeration<FileNode> {
        private Hashtable<String, FileNode> importFilesGraf = new Hashtable<String, FileNode>();

        public void addFileNode(String name, String[] dependsOn) {
            FileNode node = new FileNode();
            node.name = name;
            if (dependsOn != null) {
                for (String dependOn : dependsOn) {
                    node.dependOn.add(dependOn);
                }
            }
            importFilesGraf.put(name, node);
        }

        /**
         * Проверка на зацикливание
         */
        public void check() {
            //Цикл по элементам графа
            for (FileNode node : importFilesGraf.values()) {
                //Проверка каждого элемента графа
                check(node, new Stack<String>());
            }
        }

        /**
         * Рекурсивная функция проверки зависимости импортируемых файлов на зацикливание зависимостей
         * @param node
         * @param stack
         */
        private void check(FileNode node, Stack<String> stack) {
            stack.push(node.name);
            for (String dependOn : node.dependOn) {
                if (stack.contains(dependOn)) {
                    throw new FatalException("Check dependenses import data files fail. Found cyclic on file "
                            + stack.toString());
                } else {
                    check(importFilesGraf.get(dependOn), stack);
                }
            }
            stack.pop();
        }

        @Override
        public boolean hasMoreElements() {
            //Поиск хотя бы одного не импортированного
            for (FileNode node : importFilesGraf.values()) {
                if (!node.isLoad) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public FileNode nextElement() {
            FileNode result = null;
            // Поиск первого не импортированного
            for (FileNode node : importFilesGraf.values()) {
                if (!node.isLoad) {
                    //Нашли не импортированного, получаем по цепочке зависимостей ближайший не импортированный
                    result = getDependOn(node);
                    break;
                }
            }
            result.isLoad = true;
            return result;
        }

        /**
         * Поднимаемся по ветке зависимых файлов и ищем наиболее верхние не загруженные
         * @param node
         * @return
         */
        private FileNode getDependOn(FileNode node) {
            FileNode result = null;
            //Элемент верхнего уровня, который не от кого не зависит
            if (node.dependOn.size() == 0) {
                //Проверяем его на загруженность
                if (!node.isLoad) {
                    result = node;
                }
            } else {
                //Элемент не верхнего уровня, проверяем загруженность элементов верхнего уровня
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
