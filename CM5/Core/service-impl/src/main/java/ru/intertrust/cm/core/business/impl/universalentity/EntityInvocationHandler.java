package ru.intertrust.cm.core.business.impl.universalentity;

import java.lang.reflect.Proxy;
import java.util.Date;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.universalentity.Entity;

class EntityInvocationHandler extends ComponentInvocationHandler implements Entity {

    private static final long serialVersionUID = 1L;

    @Nonnull
    static Object createProxy (final @Nonnull Class<?> clazz, final @Nonnull DomainObject dop) {
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new EntityInvocationHandler(dop));
    }

    EntityInvocationHandler (final @Nonnull DomainObject dop) {
        super("", new DomainObjectContainer(dop));
    }

    @Override
    public boolean isNew () {
        return this.cnt.getDomainObject().isNew();
    }

    @Override
    public Id getId () {
        this.checkNotNew();
        return this.cnt.getDomainObject().getId();
    }

    @Override
    public Date getCreatedDate () {
        this.checkNotNew();
        return this.cnt.getDomainObject().getCreatedDate();
    }

    @Override
    public Date getModifiedDate () {
        this.checkNotNew();
        return this.cnt.getDomainObject().getModifiedDate();
    }

    @Override
    public void save () {
        this.cnt.setDomainObject(BeansHolder.get().crud.save(this.cnt.getDomainObject()));
        this.onAfterSave();
    }

    @Override
    protected boolean isInvokeOnSelf (final @Nonnull Class<?> declaringClazz) {
        return declaringClazz == Entity.class || super.isInvokeOnSelf(declaringClazz);
    }

    private void checkNotNew () {
        if (this.isNew()) {
            throw new IllegalStateException("entity is new");
        }
    }

}