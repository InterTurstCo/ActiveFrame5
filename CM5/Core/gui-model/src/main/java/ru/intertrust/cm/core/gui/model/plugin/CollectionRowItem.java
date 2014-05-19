package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class  CollectionRowItem implements Dto{
    private Id id;
    private  HashMap<String, Value> row;

    public CollectionRowItem() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getStringValue(String key) {
        Value value = row.get(key);
        return value == null || value.get() == null ? "" : value.toString();
    }

    public Value getRowValue(String key) {
        return row.get(key);
    }

    public void setRow(HashMap<String, Value> row) {
        this.row = row;
    }

    public HashMap<String, Value> getRow () {
        return row;
    }

   /* @Override
    public int hashCode() {
        return getId() == null ? System.identityHashCode(this) : getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null || !(getClass() == obj.getClass())) {
            return false;
        }
        CollectionRowItem other = (CollectionRowItem) obj;
        return (getId() == null ? other.getId() == null : getId().equals(other.getId()));
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionRowItem that = (CollectionRowItem) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (row != null ? !row.values().equals(that.row.values()) : that.row != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (row != null ? row.hashCode() : 0);
        return result;
    }
       public static void main(String[] args) {

           HashMap<String, Value> one = new HashMap<>();
           one.put("1", new StringValue("1"));
           HashMap<String, Value> two = new HashMap<>();
           two.put("1", new StringValue("1"));
           System.out.println("" + one.equals(two));
           CollectionRowItem collectionRowItem = new CollectionRowItem();
           collectionRowItem.setId(new RdbmsId(2, 5L));
           collectionRowItem.setRow(one);
           List<CollectionRowItem> list = new ArrayList<>();
           list.add(collectionRowItem) ;
           CollectionRowItem collectionRowItem2 = new CollectionRowItem();
           collectionRowItem2.setId(new RdbmsId(2, 5L));
           collectionRowItem2.setRow(one);

           Set<CollectionRowItem> set = new LinkedHashSet<>();
           set.add(collectionRowItem2);
          set.addAll(list);
         //  set.add(collectionRowItem);
           System.out.println("size" + set.size());
       }
}
