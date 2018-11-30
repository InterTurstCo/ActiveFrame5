package ru.intertrust.cm.core.business.impl.universalentity;

import java.util.Collection;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.universalentity.EnumProcessor;
import ru.intertrust.cm.core.business.api.dto.universalentity.NonAbstract;
import ru.intertrust.cm.core.util.Args;
import ru.intertrust.cm.core.util.PackagesScanner;

public abstract class ConfigurationSupport {

    public static void readConfiguration (final @Nonnull String... packageNames) {
        (new PackagesScanner()).addPackages(Args.notNull(packageNames, "packageNames")).addAnnotationFinder(NonAbstract.class, new Processor()).scan();
    }

    @Nonnull
    static <T> EnumProcessor<T> getEnumProcessor (final @Nonnull Class<T> clazz) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Nonnull
    static ClassMeta getClassMeta (final @Nonnull String dopType) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Nonnull
    static ClassMeta getClassMeta (final @Nonnull Class<?> clazz) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Nonnull
    static Collection<Collection<String>> getUniqueConstraints (final @Nonnull Class<?> clazz) {
        throw new UnsupportedOperationException(); // TODO
    }

    private static class Processor implements PackagesScanner.ClassFoundCallback {

        @Override
        public void processClass (final Class<?> foundClass) {
            throw new UnsupportedOperationException(); // TODO
        }

    }

    private ConfigurationSupport () {
    }

}