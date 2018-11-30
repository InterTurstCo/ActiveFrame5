package ru.intertrust.cm.core.business.impl.universalentity;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.universalentity.Entity;
import ru.intertrust.cm.core.business.api.dto.universalentity.EntityDao;
import ru.intertrust.cm.core.business.api.dto.universalentity.ListElement;
import ru.intertrust.cm.core.business.api.dto.universalentity.NonAbstract;
import ru.intertrust.cm.core.util.Args;

public class EntityDaoStdImpl implements EntityDao {

    @Override
    public <T extends Entity> T create (final Class<T> clazz) {

        final ClassMeta clazzMeta = ConfigurationSupport.getClassMeta(clazz);

        if (clazzMeta.clazzType == ClassMeta.ClassType.ENTITY_NON_ABSTRACT) {
            return clazz.cast(EntityInvocationHandler.createProxy(clazz, BeansHolder.get().crud.createDomainObject(clazzMeta.dopType)));
        } else {
            throw new RuntimeException("'" + clazz + "' must be annotated by '" + NonAbstract.class + "' and must extends '" + Entity.class + "'");
        }

    }

    @Override
    public Entity find (final Id id) {

        final DomainObject dop = BeansHolder.get().crud.find(Args.notNull(id, "id"));
        final ClassMeta clazzMeta = ConfigurationSupport.getClassMeta(dop.getTypeName());

        if (clazzMeta.clazzType == ClassMeta.ClassType.ENTITY_NON_ABSTRACT) {
            return (Entity)EntityInvocationHandler.createProxy(clazzMeta.clazz, dop);
        } else {
            throw new RuntimeException("dop-type '" + dop.getTypeName() + "' must be an non-abstract entity, see '" + clazzMeta.clazz + "'");
        }

    }

    @Override
    public void delete (final Id id) {
        BeansHolder.get().crud.delete(Args.notNull(id, "id"));
    }

    @Override
    public <E extends ListElement> E createListElement (final List<E> list) {

        if (!(Args.notNull(list, "list") instanceof ListWrapperLe<?>)) {
            throw new RuntimeException("unmanaged list instance of '" + list.getClass() + "'");
        }

        final ListWrapperLe<E> wrapper = (ListWrapperLe<E>)list;
        return wrapper.createElement();

    }

}