package ru.intertrust.cm.core.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.apache.commons.logging.Log;

/**
 * Класс для расширения стандартного classLoader для загрузки классов
 * скриптлетов
 */
public class ScriptletClassLoader extends ClassLoader {
	private Hashtable<String, byte[]> m_classes;
	private String scriptletFolderPath;

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

		m_classes = new Hashtable<String, byte[]>();
		File scriptletFolder = new File(scriptletFolderPath);
		File[] scriptletClasseFiles = scriptletFolder
				.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith("class");
					}
				});

		for (int i = 0; i < scriptletClasseFiles.length; i++) {
			String className = scriptletClasseFiles[i].getName().substring(0,
					scriptletClasseFiles[i].getName().length() - 6);
			m_classes.put(className, loadClassData(scriptletClasseFiles[i]));
		}
	}

	/**
	 * Поиск класса. Сначала ищем класс в скриптлетах, и если его нет то ищем в
	 * родительском classloader
	 * 
	 * @param name
	 *            String
	 * @return Class
	 * @throws ClassNotFoundException
	 */
	@Override
	protected Class findClass(String name) throws ClassNotFoundException {

		Class result = null;
		if (!m_classes.keySet().contains(name)) {
			result = super.findClass(name);
		} else {
			byte[] b = (byte[]) m_classes.get(name);
			result = defineClass(name, b, 0, b.length);
		}
		return result;
	}

	@Override
	protected URL findResource(String name) {
		try {

			String className = name.substring(0, name.length() - 6);
			URL result = null;
			if (!m_classes.keySet().contains(className)) {
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
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = new FileInputStream(name);
			out = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			int size = 0;
			while ((size = in.read(buf)) > 0) {
				out.write(buf, 0, size);
			}
			return out.toByteArray();
		} catch (Exception ex) {
			throw new RuntimeException("Can not load class file", ex);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ignoreEx) {
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception ignoreEx) {
			}
		}
	}
}
