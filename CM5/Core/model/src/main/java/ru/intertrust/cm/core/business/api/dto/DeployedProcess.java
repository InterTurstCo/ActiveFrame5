package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;


public class DeployedProcess implements Dto{
	private String id;
	private String name;
	private Date deployedTime;
	private String category;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDeployedTime() {
		return deployedTime;
	}
	public void setDeployedTime(Date deployedTime) {
		this.deployedTime = deployedTime;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

}
