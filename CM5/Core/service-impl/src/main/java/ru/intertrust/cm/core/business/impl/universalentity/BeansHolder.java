package ru.intertrust.cm.core.business.impl.universalentity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.universalentity.EntityDao;

@Configurable
class BeansHolder {

    private static final ThreadLocal<BeansHolder> thBeansHolders = new ThreadLocal<BeansHolder>() {

        @Override
        protected BeansHolder initialValue () {
            return new BeansHolder();
        }

    };

    @Nonnull
    static BeansHolder get () {
        return thBeansHolders.get();
    }

    @Nonnull
    @Inject
    EntityDao entityDao;

    @Nonnull
    @Inject
    CrudService crud;

    @Nonnull
    @Inject
    CollectionsService colls;

    private BeansHolder () {
    }

}