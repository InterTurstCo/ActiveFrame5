package ru.intertrust.cm.core.business.api.dto;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vmatsukevich
 * Date: 12/2/13
 * Time: 10:45 AM
 */
public class IdsIncludedFilter extends IdBasedFilter {

    public IdsIncludedFilter() {
    }

    public IdsIncludedFilter(Filter filter) {
        super(filter);
    }

    public IdsIncludedFilter(List<ReferenceValue> idsIncluded) {
        super(idsIncluded);
    }

    public IdsIncludedFilter(ReferenceValue... idsIncluded) {
        super(idsIncluded);
    }

    @Override
    public IdsIncludedFilter clone() {
        return (IdsIncludedFilter) super.clone();
    }
}
