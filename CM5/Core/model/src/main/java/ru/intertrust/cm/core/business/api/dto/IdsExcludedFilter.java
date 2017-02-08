package ru.intertrust.cm.core.business.api.dto;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vmatsukevich
 * Date: 12/2/13
 * Time: 10:45 AM
 */
public class IdsExcludedFilter extends IdBasedFilter {

    public IdsExcludedFilter() {
    }

    public IdsExcludedFilter(Filter filter) {
        super(filter);
    }

    public IdsExcludedFilter(List<ReferenceValue> idsIncluded) {
        super(idsIncluded);
    }

    public IdsExcludedFilter(ReferenceValue... idsIncluded) {
        super(idsIncluded);
    }

    @Override
    public IdsExcludedFilter clone() {
        return (IdsExcludedFilter) super.clone();
    }
}
