package ru.intertrust.cm.core.gui.impl.client.temp;

import com.vaadin.ui.Table;

public class ContentTable {
	
	
	
	public  Table renderTable(){
		Table table = new Table("Обувной прайс");

		table.addContainerProperty("Фирма", String.class, null);
		table.addContainerProperty("Цвет", String.class, null);
		table.addContainerProperty("Цена", Integer.class, null);
		
		
		Object[] listItems = new Object[6];
		listItems[0] = new Object[] { "Найки", "Серые", 100 };
		listItems[1] = new Object[] { "Найки", "Серые", 100 };
		listItems[2] = new Object[] { "Рибоки", "Черные", 85 };
		listItems[3] = new Object[] { "Рибоки", "Черные", 85 };
		listItems[4] = new Object[] { "Риабеки", "Синие", 50 };
		listItems[5] = new Object[] { "Риабеки", "Синие", 50 };
		
		
		for(int i = 0;i < listItems.length;i++){
			table.addItem((Object[]) listItems[i],new Integer(i));
		}	
		return table;
	}
}
