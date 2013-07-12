package ru.intertrust.cm.core.gui.impl.client.temp;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public class Header {
	public static Integer headerWidth;
	//87
	public static Integer omniboxHeight = 88;
	public static Integer getHeightHeader(){
		return headerWidth;
	}
public static void basic(HorizontalLayout layout) {
		
		MenuBar barmenu = new MenuBar();
		layout.addComponent(barmenu);

		
		final Label selection = new Label("менюшка");
		layout.addComponent(selection);

		
		MenuBar.Command mycommand = new MenuBar.Command() {
			private static final long serialVersionUID = 4483012525105015694L;

			public void menuSelected(MenuItem selectedItem) {
				selection.setValue("Выбран пункт меню: " + selectedItem.getText());
			}
		};

		MenuBar.MenuItem all = barmenu.addItem("Все", null, null);
		all.addItem("По номеру", null, mycommand);
		all.addItem("По типу", null, mycommand);
		all.addItem("По отв. исполнителю", null, mycommand);
		all.addItem("По корреспонденту", null, mycommand);
		
		MenuBar.MenuItem inWork = barmenu.addItem("В работе", null, null);
		inWork.addItem("По номеру", null, mycommand);
		inWork.addItem("По типу", null, mycommand);
		inWork.addItem("По отв. исполнителю", null, mycommand);
		inWork.addItem("По корреспонденту", null, mycommand);
		inWork.addItem("По контролеру", null, mycommand);
		inWork.addItem("По инициатору", null, mycommand);
		inWork.addItem("По сроку", null, mycommand);
		
		MenuBar.MenuItem end = barmenu.addItem("Завершенные", null, null);
		end.addItem("По номеру", null, mycommand);
		end.addItem("По типу", null, mycommand);
		end.addItem("По отв. исполнителю", null, mycommand);
		end.addItem("По корреспонденту", null, mycommand);
		end.addItem("По контролеру", null, mycommand);
		end.addItem("По инициатору", null, mycommand);
		end.addItem("По дате завершения", null, mycommand);
		
		
		MenuBar.MenuItem keys = barmenu.addItem("Проекты кейсов", null, null);
		keys.addItem("По типу", null, mycommand);
		keys.addItem("По отв. исполнителю", null, mycommand);
		keys.addItem("По инициатору", null, mycommand);
		
		MenuBar.MenuItem tasks = barmenu.addItem("Стадии и задачи", null, null);
		tasks.addItem("В работе", null, mycommand);
		tasks.addItem("В плане", null, mycommand);
		
		
		MenuBar.MenuItem delete = barmenu.addItem("Корзина", null, mycommand);
		
		headerWidth = (int) barmenu.getHeight()+(int)selection.getHeight()+omniboxHeight;
				
	}
}
