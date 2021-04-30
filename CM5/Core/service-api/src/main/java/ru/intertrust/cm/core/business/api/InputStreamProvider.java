package ru.intertrust.cm.core.business.api;

import java.io.InputStream;

/**
 * This interface has a single method, that returns an opened {@link InputStream}.
 * It's necessary for give to called a method opportunity to get the {@link InputStream} and close it inside.
 */
@FunctionalInterface
public interface InputStreamProvider {
    InputStream getInputStream();
}
