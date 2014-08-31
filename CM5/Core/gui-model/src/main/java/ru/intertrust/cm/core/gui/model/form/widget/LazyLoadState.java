package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 25.08.2014
 *         Time: 11:05
 */
public class LazyLoadState implements Dto {
    private int pageSize;
    private int offset;
    public LazyLoadState() {
    }

    public LazyLoadState(int pageSize, int offset) {
        this.pageSize = pageSize;
        this.offset = offset;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void onNextPage(){
        offset += pageSize;
    }

}
