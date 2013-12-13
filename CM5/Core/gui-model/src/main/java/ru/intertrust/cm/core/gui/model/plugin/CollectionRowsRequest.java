package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 21.11.13
 * Time: 12:46
 * To change this template use File | Settings | File Templates.
 */
public class CollectionRowsRequest implements Dto {
    private int offset;
    private int limit;
    private String collectionName;
    private HashMap<String, String> fields = new HashMap<String, String>();
    private boolean sotrType;
    private String columnName;
    private boolean sortable;
    private String field;

    public CollectionRowsRequest(int offset, int limit, String collectionName, HashMap<String, String> fields) {
        this.offset = offset;
        this.limit = limit;
        this.collectionName = collectionName;
        this.fields = fields;

    }

    public CollectionRowsRequest(int offset, int limit, String collectionName, HashMap<String, String> fields,
                                 boolean sotrType, String columnName, String field) {
        this.offset = offset;
        this.limit = limit;
        this.collectionName = collectionName;
        this.fields = fields;
        this.sotrType = sotrType;
        this.columnName = columnName;
        this.sortable = true;
        this.field = field;
    }

    public CollectionRowsRequest() {
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setFields(HashMap<String, String> fields) {
        this.fields = fields;
    }

    public HashMap<String, String> getFields() {
        return fields;
    }

    public boolean isSotrType() {
        return sotrType;
    }

    public void setSotrType(boolean sotrType) {
        this.sotrType = sotrType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
