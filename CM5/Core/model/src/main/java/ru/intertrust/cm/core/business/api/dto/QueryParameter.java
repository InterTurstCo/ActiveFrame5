package ru.intertrust.cm.core.business.api.dto;

import org.simpleframework.xml.Attribute;

public class QueryParameter implements Dto{
    private static final long serialVersionUID = -4602772199170498622L;
    @Attribute
	private String query;

	public QueryParameter(){
	    
	}

    public QueryParameter(String query){
        this.query = query;
    }
	
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
}
