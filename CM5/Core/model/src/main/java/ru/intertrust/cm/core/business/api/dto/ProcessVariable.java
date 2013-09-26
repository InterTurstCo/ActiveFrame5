package ru.intertrust.cm.core.business.api.dto;

/**
 * Переменная процесса
 * @author larin
 *
 */
public class ProcessVariable implements Dto{
	private static final long serialVersionUID = 5925388430906168782L;
	private String name;
	private Dto value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Dto getValue() {
		return value;
	}
	public void setValue(Dto value) {
		this.value = value;
	}
}
