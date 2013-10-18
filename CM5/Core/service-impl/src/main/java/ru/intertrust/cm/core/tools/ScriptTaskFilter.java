package ru.intertrust.cm.core.tools;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
/**
 * Класс для хранения названий и параметров фильтров. Применяется в ScriptTask процессов Activity
 * @author lyakin
 *
 */
public class ScriptTaskFilter {
	private Hashtable<String, Object> filters;
	
	/**
	 * Конструктор класса. Инициализация Hashtable, в котором хранятся названия и параметры фильтров
	 */
	public ScriptTaskFilter() {
		filters = new Hashtable<>();
	}
	/**
	 * Добавление фильтра с неограниченным числом параметров
	 * @param filterName - имя фильтра
	 * @param parameters - параметры
	 */
	public void add(String filterName, Object ... parameters){
		int length = parameters.length;
		//Если в массиве parameters несколько параметров, то создаем List 
		if (length>1){
			List<Object> list = new ArrayList<>();
			for (int i=0;i<length;i++){
				list.add(parameters[i]);
			}
			filters.put(filterName, list);
		}
		//Если в массиве parameters 1 элемент, кладем его в фильтр, как Object
		else{
			filters.put(filterName, parameters[0]);
		}
	}

	public Enumeration<String> getFiltersNames(){
		return filters.keys();
	}
	
	/**
	 * Преобразование <filters> в ru.intertrust.cm.core.business.api.dto.Filter и помещение их в List
	 * @return
	 */
	public List<Filter> getFiltersList(){
		List<Filter> filtersList = new ArrayList<>();
		String key;
        Object value;
		Enumeration<String> enm = getFiltersNames();
		while (enm.hasMoreElements()) {
			Filter filter = new Filter();
			key = (String)enm.nextElement();
			value = (Object) filters.get(key);
			filter.setFilter(key);
			if (value instanceof Id){
                //Создаем критерий фильтра
                ReferenceValue rv = new ReferenceValue((Id)value);
                filter.addCriterion(0, rv);
        	}else if (value instanceof String){
                //Создаем критерий фильтра
                StringValue sv = new StringValue((String)value);
                filter.addCriterion(0, sv);
        	}else if (value instanceof Double){
                //Создаем критерий фильтра
                LongValue dv = new LongValue(((Double) value).longValue());
                filter.addCriterion(0, dv);
        	}else if (value instanceof List){
        		//TODO если приходит массив сделать разбор
        	}
			filtersList.add(filter);		
		}
		return filtersList;
	}
}
