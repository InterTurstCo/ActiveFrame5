package ru.intertrust.cm.test.collection.generator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

/**
 * Тестовый генератор коллекции
 * @author atsvetkov
 *
 */
@ServerComponent(name = "employee.collection.test")
public class EmployeesCollectionGenerator implements CollectionDataGenerator {

    private IdentifiableObjectCollection collection;

    @Autowired
    private CollectionsService collectionService;

    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {

        this.collection = collectionService.findCollection("Employees", sortOrder, filters, 0, 0);
        return collection;
    }

    @Override
    public int findCollectionCount(List<? extends Filter> filterValues) {
        return findCollection(filterValues, null, 0, 0).size();
    }
}
