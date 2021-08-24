package ru.intertrust.cm.core.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Hashtable;

/**
 * Класс для расширения стандартного classLoader для загрузки классов
 * скриптлетов
 */
public class ScriptletClassLoader extends ClassLoader {
	private final Hashtable<String, byte[]> m_classes;
	private final String scriptletFolderPath;

	/**
	 * Конструктор принимает директорию где могут быть скриптлеты и загружает
	 * файлы с расширением *.class
	 * 
	 * @param scriptletFolderPath
	 *            String
	 */
	public ScriptletClassLoader(String scriptletFolderPath,
			ClassLoader defaultClassLoader) {
		super(defaultClassLoader);

		this.scriptletFolderPath = scriptletFolderPath;

		m_classes = new Hashtable<>();
		File scriptletFolder = new File(scriptletFolderPath);
		File[] scriptletClassFiles = scriptletFolder.listFiles((dir, name) -> name.endsWith("class"));

		if (scriptletClassFiles == null) {
			return;
		}

		for (final File scriptletClassFile : scriptletClassFiles) {
			String className = scriptletClassFile.getName().substring(0,
					scriptletClassFile.getName().length() - 6);
			m_classes.put(className, loadClassData(scriptletClassFile));
		}
	}

	/**
	 * Поиск класса. Сначала ищем класс в скриптлетах, и если его нет то ищем в
	 * родительском classloader
	 *
	 * @see ClassLoader#findClass(String) for more information
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		Class<?> result;
		if (!m_classes.containsKey(name)) {
			result = super.findClass(name);
		} else {
			byte[] b = m_classes.get(name);
			result = defineClass(name, b, 0, b.length);
		}
		return result;
	}

	@Override
	protected URL findResource(String name) {
		try {

			String className = name.substring(0, name.length() - 6);
			URL result;
			if (!m_classes.containsKey(className)) {
				result = super.findResource(name);
			} else {
				result = new URL("file:///" + scriptletFolderPath + "/" + name);
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException("Error find resource", ex);
		}
	}

	/**
	 * Зачитывает класс из файла
	 * 
	 * @param name
	 *            File
	 * @return byte[]
	 */
	private byte[] loadClassData(File name) {
		try (FileInputStream in = new FileInputStream(name); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buf = new byte[256];
			int size;
			while ((size = in.read(buf)) > 0) {
				out.write(buf, 0, size);
			}
			return out.toByteArray();
		} catch (Exception ex) {
			throw new RuntimeException("Can not load class file", ex);
		}
	}
}
