package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Объект данных дочерней коллекции.
 * <br><br>
 * Created by Myskin Sergey on 17.05.2019.
 */
public class ChildCollectionColumnData implements Dto {

    /**
     * Флаг означающий посчитано ли уже количество дочерних элементов, обычно это выполняется в основном запросе в виде отдельной колонки с COUNT
     */
    private boolean hasChildsCountAlreadyCalculated = false;
    /**
     * Флаг означающий имеет ли дочерняя коллекция хоть 1 элемент (не пустая ли она)
     */
    private boolean hasCollectionAnyChild = false;
    /**
     * Количество дочерних элементов. Если флаг {@link #hasChildsCountAlreadyCalculated} == true,<br>
     * то устанавливается уже посчитанное количество (так как уже было вычислено ранее), чтобы не уменьшать производительность,<br>
     * в противном случае оно вычисляется.
     */
    private int childCollectionItemsCount = 0;

    public ChildCollectionColumnData() {
    }

    public ChildCollectionColumnData(boolean hasChildsCountAlreadyCalculated, boolean hasCollectionAnyChild, int childCollectionItemsCount) {
        this.hasChildsCountAlreadyCalculated = hasChildsCountAlreadyCalculated;
        this.hasCollectionAnyChild = hasCollectionAnyChild;
        this.childCollectionItemsCount = childCollectionItemsCount;
    }

    public boolean isHasChildsCountAlreadyCalculated() {
        return hasChildsCountAlreadyCalculated;
    }

    public void setHasChildsCountAlreadyCalculated(boolean hasChildsCountAlreadyCalculated) {
        this.hasChildsCountAlreadyCalculated = hasChildsCountAlreadyCalculated;
    }

    public boolean isHasCollectionAnyChild() {
        return hasCollectionAnyChild;
    }

    public void setHasCollectionAnyChild(boolean hasCollectionAnyChild) {
        this.hasCollectionAnyChild = hasCollectionAnyChild;
    }

    public int getChildCollectionItemsCount() {
        return childCollectionItemsCount;
    }

    public void setChildCollectionItemsCount(int childCollectionItemsCount) {
        this.childCollectionItemsCount = childCollectionItemsCount;
    }

}
