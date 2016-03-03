package ru.intertrust.cm.core.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;

import ru.intertrust.cm.core.util.test.TestAnnotation;
import ru.intertrust.cm.core.util.test.a.A_yes;
import ru.intertrust.cm.core.util.test.a.b.c.d.D_yes;
import ru.intertrust.cm.core.util.test.a.b.c.e.E_yes;
import ru.intertrust.cm.core.util.test.k.l.m.M_yes;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PackagesScannerTest {

    @Mock private PackagesScanner.ClassFoundCallback callback;
    @Captor private ArgumentCaptor<Class<?>> classCaptor;

    @Mock private PackagesScanner.Finder finder;
    @Captor private ArgumentCaptor<Resource> resourceCaptor;

    @Test
    public void testAnnotationScan() {
        PackagesScanner testee = new PackagesScanner()
                .addPackage("ru.intertrust.cm.core.util.test.a.b.c.d")
                .addPackage("ru.intertrust.cm.core.util.test.k.l")
                .addAnnotationFinder(TestAnnotation.class, callback);

        testee.scan();

        verify(callback, times(2)).processClass(classCaptor.capture());
        assertThat(classCaptor.getAllValues(), Matchers.<Class<?>>containsInAnyOrder(D_yes.class, M_yes.class));
    }

    @Test
    public void testAnnotationScanIntersectingPaths() {
        PackagesScanner testee = new PackagesScanner().addPackages(
                "ru.intertrust.cm.core.util.test.a.b.c.d",
                "ru.intertrust.cm.core.util.test.a")
                .addAnnotationFinder(TestAnnotation.class, callback);

        testee.scan();

        verify(callback, times(3)).processClass(classCaptor.capture());
        assertThat(classCaptor.getAllValues(),
                Matchers.<Class<?>>containsInAnyOrder(A_yes.class, D_yes.class, E_yes.class));
    }

    @Test
    public void testFinder() {
        PackagesScanner testee = new PackagesScanner()
                .addPackage("ru.intertrust.cm.core.util.test.k")
                .addFinder(finder, callback);

        testee.scan();

        verify(finder, times(2))
                .checkResource(any(MetadataReader.class), resourceCaptor.capture(), any(ClassLoader.class));
        assertThat(getNames(resourceCaptor.getAllValues()),
                Matchers.containsInAnyOrder("M_yes.class", "N_no.class"));
    }

    @Test
    public void testFinderIntersectingPaths() {
        PackagesScanner testee = new PackagesScanner().addPackages(
                "ru.intertrust.cm.core.util.test.a.b",
                "ru.intertrust.cm.core.util.test.a.b.c.d",
                "ru.intertrust.cm.core.util.test.a")
                .addFinder(finder, callback);

        testee.scan();

        verify(finder, times(4))
                .checkResource(any(MetadataReader.class), resourceCaptor.capture(), any(ClassLoader.class));
        assertThat(getNames(resourceCaptor.getAllValues()),
                Matchers.containsInAnyOrder("A_yes.class", "D_no.class", "D_yes.class", "E_yes.class"));
    }

    private List<String> getNames(List<Resource> values) {
        ArrayList<String> names = new ArrayList<>(resourceCaptor.getAllValues().size());
        for (Resource value : resourceCaptor.getAllValues()) {
            names.add(value.getFilename());
        }
        return names;
    }
}
