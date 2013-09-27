package ru.intertrust.cm.core.dao.impl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;
import ru.intertrust.cm.core.dao.impl.extension.ExtentionInvocationHandler;
import ru.intertrust.cm.core.model.ExtensionPointException;

/**
 * Имплементация сервиса точек расширения.
 * 
 * @author larin
 * 
 */
public class ExtensionServiceImpl implements ExtensionService,
		BeanDefinitionRegistryPostProcessor {
	/**
	 * Реестр точек расширения
	 */
	private static Hashtable<Class<? extends ExtentionInvocationHandler>, Hashtable<String, List<ExtensionPointHandler>>> reestr;
	/**
	 * Базовый пакет, в котором ищутся классы точек расширения
	 */
	private String basePackage;

	/**
	 * Получение точки расширения по имени интерфейса точки расширения и
	 * фильтру. Фильтр может быть null в этом случае возвратятся все точки
	 * расширения для переданного интерфейса
	 */
	@Override
	public ExtensionPointHandler getExtentionPoint(
			Class<? extends ExtensionPointHandler> extentionPointInterface,
			String filter) {
		ExtentionInvocationHandler handler = new ExtentionInvocationHandler(
				this, filter);
		ExtensionPointHandler proxy = (ExtensionPointHandler) Proxy
				.newProxyInstance(extentionPointInterface.getClassLoader(),
						new Class[] { extentionPointInterface }, handler);
		return proxy;
	}

	/**
	 * Инициализация точек расширения, путем сканирования всех классов,
	 * аннотированых ExtensionPoint аннотацией и заполнение реестра
	 */
	private void init() {
		try {
			reestr = new Hashtable<Class<? extends ExtentionInvocationHandler>, Hashtable<String, List<ExtensionPointHandler>>>();

			// Сканирование класспаса
			ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
					true);
			scanner.addIncludeFilter(new AnnotationTypeFilter(
					ExtensionPoint.class));
			// Цикл по найденным классам
			for (BeanDefinition bd : scanner
					.findCandidateComponents(basePackage)) {
				String className = bd.getBeanClassName();
				// Получение найденного класса
				Class extentionPointClass = Class.forName(className);
				// Получение анотации ExtensionPoint
				ExtensionPoint annatation = (ExtensionPoint) extentionPointClass
						.getAnnotation(ExtensionPoint.class);

				// Проверка наличия анотации в классе
				if (annatation != null) {

					// Получение интерфейса, который имплементит точка
					// расширения, интерфейс должен быть наследником
					// ExtensionPointHandler
					Class[] interfaces = extentionPointClass.getInterfaces();
					Class interfaceClass = null;
					for (Class item : interfaces) {
						if (ExtensionPointHandler.class.isAssignableFrom(item)) {
							interfaceClass = item;
							break;
						}
					}

					// Формируем сообщение об ошибке, если класс анотированный
					// ExtensionPoint не имплементит интерфейс, отнаследованный
					// от ExtensionPointHandler
					if (interfaceClass == null)
						throw new ExtensionPointException(
								"Class "
										+ extentionPointClass
										+ " need implements ExtensionPointHandler child interface.");
					// Получаем фильтр из аннотации
					String filter = annatation.filter();
					// Создаем экзампляр точки расширения
					ExtensionPointHandler extentionPoint = (ExtensionPointHandler) extentionPointClass
							.newInstance();
					// Сохраняем точку расширения в реестр
					Hashtable<String, List<ExtensionPointHandler>> oneTypeExtensions = reestr
							.get(interfaceClass);
					if (oneTypeExtensions == null) {
						oneTypeExtensions = new Hashtable<String, List<ExtensionPointHandler>>();
						reestr.put(interfaceClass, oneTypeExtensions);
					}
					List<ExtensionPointHandler> filteredExtension = oneTypeExtensions
							.get(filter);
					if (filteredExtension == null) {
						filteredExtension = new ArrayList<ExtensionPointHandler>();
						oneTypeExtensions.put(filter, filteredExtension);
					}
					filteredExtension.add(extentionPoint);
				}
			}
		} catch (Exception ex) {
			throw new ExtensionPointException(
					"Error on init extension point classes", ex);
		}
	}

	/**
	 * Получает список точек расширения определенного интерфейса
	 */
	public List<ExtensionPointHandler> getExtentionPointList(
			Class<? extends ExtensionPointHandler> extentionPointInterface,
			String filter) {
		List<ExtensionPointHandler> result = new ArrayList<ExtensionPointHandler>();

		// Получаем все хандлеры точек расширения по интерфейсу
		Map<String, List<ExtensionPointHandler>> oneTypeExtensions = reestr
				.get(extentionPointInterface);

		// Проверка на наличие точек расширения определенного интерфейса
		if (oneTypeExtensions != null) {
			// Проверка на переданный фильтр точек расширения, в пределах одного
			// интерфейса
			if (filter != null) {
				// Получение точек расширения по фильтру
				List<ExtensionPointHandler> filteredExtension = oneTypeExtensions
						.get(filter);
				// Если точки расширения по фильтру найдены то добавляем их в
				// результат
				if (filteredExtension != null && filteredExtension.size() > 0) {
					result.addAll(filteredExtension);
				}

				// Проверяем наличие обработчиков точек расширения, в которых не
				// указали фильтр, если такие есть то добавляем их при любом
				// значение переданного фильтра
				if (oneTypeExtensions.containsKey("")) {
					result.addAll(oneTypeExtensions.get(""));
				}
			} else {
				// Если фильтр не указан то получаем обработчики
				// зарегистрированные со всеми значениями фильтрв и добавляем их
				// в результат
				for (List<ExtensionPointHandler> extentionPoints : oneTypeExtensions
						.values()) {
					result.addAll(extentionPoints);
				}

			}
		}
		return result;
	}

	/**
	 * Метод вызывается после создания фабрики бинов. Используется для
	 * инициализации реестра обработчиков точек расширения
	 */
	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// Вызов инициализации
		init();

	}

	@Override
	public void postProcessBeanDefinitionRegistry(
			BeanDefinitionRegistry registry) throws BeansException {
		// TODO Auto-generated method stub

	}

	/**
	 * Установка базовового пакета, в котором производится поиск точек
	 * расширения
	 * 
	 * @param basePackage
	 */
	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}
}
