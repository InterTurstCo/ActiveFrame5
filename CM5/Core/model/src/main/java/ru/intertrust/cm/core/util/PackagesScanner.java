package ru.intertrust.cm.core.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import ru.intertrust.cm.core.model.FatalException;

/**
 * Класс предназначен для поиска классов по формальным признакам (например, наличию конкретной аннотации)
 * в заданных пакетах (packages), включая дочерние пакеты (subpackages).
 * 
 * PackagesScanner использует подключаемые компоненты для выполнения фильтрации классов по различным признакам.
 * Эти компоненты вызываются для каждого класса, проверяя его на соответствие каким-либо условиям.
 * Кроме того, в классе имеется конкретная реализация поискового компонента - {@link AnnotationFinder},
 * осуществляющего фильтрацию по наличию аннотации.
 * Клиенты класса могут разработать собственные поисковые компоненты, осуществляющие фильтрацию по иным признакам.
 * 
 * PackagesScanner не возвращает массив или список найденных классов; вместо этого он использует метод
 * обратного вызова (callback), который вызывается при нахождении каждого класса, удовлетворяющего условиям.
 * Интерфейс с этим методом ({@link ClassFoundCallback}) должен быть реализован клиентом.
 * 
 * Концепция PackagesScanner позволяет использовать его для одновременного поиска классов по различным признакам
 * (хотя и в одном и том же наборе пакетов), подключая несколько разных поисковых компонент.
 * Каждому фильтру сопоставляется свой callback-метод, вызываемый при нахождении класса по соответствующему условию.
 * При нахождении класса, удовлетворяющего нескольким условиям, будут вызываны все соответствующие callback-методы.
 * Такой подход позволяет объединить несолько разных поисков в один, что существенно экономит время на долгой процедуре
 * перебора большого количества классов в разветвлённой иерархии пакетов.
 * 
 * Ещё одна оптимизация перебора пакетов реализована внутри PackagesScanner. Он исключает многократный перебор
 * классов в одних и тех же пакетах, если заданный список пакетов содержит пересечения. Это делает данный класс
 * эффективным при использовании со списками пакетов, полученными из конфигурационных файлов модулей (cm-module.xml).
 * Простое склеивание списков пакетов из разных модулей может содержать совпадающие или пересекающиеся пакеты
 * с высокой долей вероятности, поскольку модули могут создаваться различными командами разработчиков.
 * 
 * Класс реализован по паттерну Builder, для его инициализации используется цепочка вызовов методов addXxx().
 * Главный метод класса, реализующий собственно поиск, - {@link #scan()}.
 * Экземпляры класса рассчитаны на однократное использование; для нового поиска следует создать новый экземпляр.
 * В процессе сканирования пакетов PackageScanner собирает статистику (количество просмотренных и найденных классов,
 * время выполнения и др.), которую можно получить методом {@link #getStatistics()}.
 * 
 * @author apirozhkov
 */
public class PackagesScanner {

    private ArrayList<String> packageList = new ArrayList<>();
    private ArrayList<Listener> listeners = new ArrayList<>();
    private Statistics statistics;

    /**
     * Создаёт новый (чистый) экземпляр класса.
     * Для инициализации экземпляра используйте методы {@link #addPackage(String)}, {@link #addPackages(String...)},
     * {@link #addPackages(Iterable)}, {@link #addFinder(Finder, ClassFoundCallback)} и
     * {@link #addAnnotationFinder(Class, ClassFoundCallback)}.
     */
    public PackagesScanner() {
        
    }

    /**
     * Добавляет пакет в список пакетов для поиска.
     * @param packageName имя пакета
     * @return this
     */
    public PackagesScanner addPackage(String packageName) {
        smartAddPackage(packageName);
        return this;
    }

    /**
     * Добавляет все имена пакетов в список пакетов для поиска.
     * @param packageNames имена пакетов
     * @return this
     */
    public PackagesScanner addPackages(String... packageNames) {
        return addPackages(Arrays.asList(packageNames));
    }

    /**
     * Добавляет все имена пакетов в список пакетов для поиска.
     * @param packageNames имена пакетов
     * @return this
     */
    public PackagesScanner addPackages(Iterable<String> packageNames) {
        for (String packageName : packageNames) {
            smartAddPackage(packageName);
        }
        return this;
    }

    /**
     * "Умное" добавление пакета в список.
     * Если в списке уже присутствует вышележащий пакет, добавление не производится.
     * Если в списке присутствуют дочерние пакеты, они исключаются из списка.
     * @param packageName имя пакета
     */
    private void smartAddPackage(String packageName) {
        String name = packageName.replaceAll("[.]", "/");
        for (Iterator<String> itr = packageList.iterator(); itr.hasNext(); ) {
            String storedName = itr.next();
            if (name.startsWith(storedName)) {
                return;     // superpackage already added - no need to add this one
            }
            if (storedName.startsWith(name)) {
                itr.remove();   // subpackage already added - replace it with this one
            }
        }
        packageList.add(name);
    }

    /**
     * Добавляет 
     * @param finder
     * @param callback
     * @return
     */
    public PackagesScanner addFinder(Finder finder, ClassFoundCallback callback) {
        listeners.add(new Listener(finder, callback));
        return this;
    }

    public PackagesScanner addAnnotationFinder(Class<? extends Annotation> annotationClass, ClassFoundCallback callback) {
        return addFinder(new AnnotationFinder(annotationClass), callback);
    }

    /**
     * Выполняет сканирование заданных пакетов. В процессе сканирования вызываются заданные ранее методы
     * обратного вызова (callbacks) для найденных классов.
     * Внимание! Этот метод может выполняться в течение продолжительного времени - нескольких секунд или даже минут.
     * @throws FatalException если возникла ошибка загрузки какого-либо класса
     */
    public void scan() {
        statistics = new Statistics();
        long startTime = System.currentTimeMillis();

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        ClassLoader classLoader = resourcePatternResolver.getClassLoader();

        for (String packageName : packageList) {
            String locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + packageName + "/**/*.class";
            ++statistics.scannedPaths;
            try {
                for (Resource resource : resourcePatternResolver.getResources(locationPattern)) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    ++statistics.scannedClasses;
                    for (Listener listener : listeners) {
                        if (listener.finder.checkResource(metadataReader, resource, classLoader)) {
                            Class<?> foundClass = classLoader.loadClass(metadataReader.getClassMetadata().getClassName());
                            ++statistics.foundClasses;
                            listener.callback.processClass(foundClass);
                        }
                    }
                }
            } catch (Exception e) {
                throw new FatalException("Error scanning classpath", e);
            }
        }

        long endTime = System.currentTimeMillis();
        statistics.scanTimeMillis = endTime - startTime;
    }

    /**
     * Интерфейс, используемый для callback-функций, вызываемых при 
     */
    public interface ClassFoundCallback {
        /**
         * Метод, вызываемый при нахождении класса по заданным условиям.
         * @param foundClass найденный класс
         */
        void processClass(Class<?> foundClass);
    }

    /**
     * Интерфейс, реализуемый компонентами поиска классов.
     */
    public interface Finder {
        /**
         * Метод, вызываемыый для выполнения проверки класса 
         * @param metadataReader может использоваться для чтения мета-информации из класса
         * @param resource ресурс - класс
         * @param classLoader может использоваться для загрузки класса
         * @return true, если класс удовлетворяет условиям поиска
         */
        boolean checkResource(MetadataReader metadataReader, Resource resource, ClassLoader classLoader);
    }

    /**
     * Компонент, осуществляющий поиск файла по наличию в нём заданной аннотации.
     */
    public static class AnnotationFinder implements Finder {
        private String annotationName;

        /**
         * Создаёт экземпляр поискового компонента.
         * @param annotationClass искомая аннотация
         */
        public AnnotationFinder(Class<? extends Annotation> annotationClass) {
            this.annotationName = annotationClass.getName();
        }

        @Override
        public boolean checkResource(MetadataReader metadataReader, Resource resource, ClassLoader classLoader) {
            return metadataReader.getAnnotationMetadata().hasAnnotation(annotationName);
        }
    }

    /*public static AnnotationFinder annotationFinder(Class<? extends Annotation> annotationClass) {
        return new AnnotationFinder(annotationClass);
    }*/

    private static class Listener {
        final Finder finder;
        final ClassFoundCallback callback;

        public Listener(Finder finder, ClassFoundCallback callback) {
            this.finder = finder;
            this.callback = callback;
        }
    }

    /**
     * Класс, содержащий статистику сканирования пакетов.
     */
    public static class Statistics {
        private int scannedPaths;
        private int scannedClasses;
        private int foundClasses;
        private long scanTimeMillis;

        /**
         * Количество просканированных пакетов верхнего уровня.
         * Может отличаться от количества пакетов, переданных при инициализации поискового объекта,
         * из-за исключения перекрывающихся пакетов.
         */
        public int getScannedPaths() {
            return scannedPaths;
        }
        /**
         * Количество просканированных классов.
         */
        public int getScannedClasses() {
            return scannedClasses;
        }
        /**
         * Количество найденных подходящих классов.
         * Если один и тот же класс был отобран несколькими фильтрами, он учитывается в этом параметре
         * соответствующее число раз.
         */
        public int getFoundClasses() {
            return foundClasses;
        }
        /**
         * Время работы метода {@link PackagesScanner#scan()} в миллисекундах.
         */
        public long getScanTimeMillis() {
            return scanTimeMillis;
        }
    }

    /**
     * Возвращает статистику сканирования пакетов - (последнего) вызова метода {@link PackagesScanner#scan()}.
     * @return объект статистики или null, если сканирование ещё не выполнялось
     */
    public Statistics getStatistics() {
        return statistics;
    }
}
