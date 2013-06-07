package ru.intertrust.cm.core.dao.impl.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Представляет набор функций для работы со строками
 * 
 * @author skashanski
 * 
 */
public class StrUtils {

	/**
	 * Формирует строку состоящую из списка переданных значений разделенных запятой
	 * 
	 * @param strings
	 *            список значений
	 * @param addQuotes
	 *            флаг определяющий нужно ли добавлять кавычки для значений внутри строки
	 * @return возвращает строку состоящую из списка значений
	 */
	public static String generateCommaSeparatedList(Collection<String> strings, boolean addQuotes) {

		StringBuilder result = new StringBuilder();
		for (Iterator<String> iterator = strings.iterator(); iterator.hasNext();) {

			if (addQuotes)
				result.append("\"").append(iterator.next()).append("\"");
			else
				result.append(iterator.next());

			if (iterator.hasNext())
				result.append(", ");

		}

		return result.toString();

	}

	/**
	 * Формирует строку состоящую из списка переданных значений разделенных запятой
	 * 
	 * @param strings
	 *            список значений
	 * @param beginStr
	 *            строка которая будет добавлена
	 * @param addQuotes
	 *            флаг определяющий нужно ли добавлять кавычки для значений внутри строки
	 * @return возвращает строку состоящую из списка значений
	 */
	public static String generateCommaSeparatedList(Collection<String> strings, String beginStr, boolean addQuotes) {

		StringBuilder result = new StringBuilder();
		for (Iterator<String> iterator = strings.iterator(); iterator.hasNext();) {

			if (addQuotes)
				result.append("\"").append(beginStr).append(iterator.next()).append("\"");
			else
				result.append(beginStr).append(iterator.next());

			if (iterator.hasNext())
				result.append(", ");

		}

		return result.toString();

	}

	/**
	 * Формирует строку состоящую из списка переданных значений и параметров разделенных запятой. В результате получаем
	 * строку ввида : string1=:param1, strings=:param2
	 * 
	 * @param strings
	 *            список значений
	 * param params список имен параметров           
	 * @return возвращает строку состоящую из списка значений
	 */
	public static String generateCommaSeparatedListWithParams(List<String> strings, List<String> params) {

		StringBuilder result = new StringBuilder();
		
		for (int i= 0; i <strings.size(); i++) {
			String string = strings.get(i);
			String param = params.get(i);
			result.append(string);
			result.append("=");
			result.append(":");
			result.append(param);
			
			if (i <(strings.size()-1))
				result.append(", ");
		}

		return result.toString();

	}

	/**
	 * Формитрует строку состоящую из повтряющхся строк разделенной запятой
	 * 
	 * @param value
	 *            строковое значение
	 * @param number
	 *            количетво повторений
	 * @return Строку состоящую из повторящих строковых значений
	 */
	public static String replicateCommaSeparatedString(String value, int number) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number; i++) {
			result.append(value);
			if (i != (number - 1))
				result.append(", ");
		}

		return result.toString();
	}

}
