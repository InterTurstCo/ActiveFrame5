package ru.intertrust.cm.core.config.doel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.model.LongFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldTypeConfig;
import ru.intertrust.cm.core.config.model.StringFieldConfig;
import ru.intertrust.cm.core.config.model.TopLevelConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.config.model.doel.DoelValidator;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

//@ContextConfiguration(locations = {"classpath*:/beans.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)
public class DoelValidatorTest {
/*
    @Mock
    private Configuration configuration;

    @InjectMocks
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Before
    public void prepareConfiguration() {
        MockitoAnnotations.initMocks(this);

        ArrayList<TopLevelConfig> objectConfigs = new ArrayList<>();
        when(configuration.getConfigurationList()).thenReturn(objectConfigs);
    }
*/
    @Mock
    private ConfigurationExplorer config;

    @Mock
    private ApplicationContext context;

    /* Схема тестовых типов, полей и связей:
     *   -> B -> D *-> F -> H
     * A         v  \    /
     *   -> C -> E --> G -> I
     * A: toB toC
     * B: toD bString
     * C: toE cString
     * D: toE toForG
     * E: toG
     * F: toH fString fgLong
     * G: toH toI gString fgLong
     * H: hDate
     * I: iDate
     */
    @Before
    public void prepareConfiguration() {
        MockitoAnnotations.initMocks(this);
        new SpringApplicationContext().setApplicationContext(context);
        when(context.getBean(ConfigurationExplorer.class)).thenReturn(config);

        // ================ Связи =================
        ReferenceFieldConfig refField = new ReferenceFieldConfig();
        refField.setName("toB");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("B")));
        when(config.getFieldConfig("A", "toB")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toC");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("C")));
        when(config.getFieldConfig("A", "toC")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toD");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("D")));
        when(config.getFieldConfig("B", "toD")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toE");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("E")));
        when(config.getFieldConfig("C", "toE")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toE");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("E")));
        when(config.getFieldConfig("D", "toE")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toForG");
        ArrayList<ReferenceFieldTypeConfig> types = new ArrayList<>();
        types.add(new ReferenceFieldTypeConfig("F"));
        types.add(new ReferenceFieldTypeConfig("G"));
        refField.setTypes(types);
        when(config.getFieldConfig("D", "toForG")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toG");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("G")));
        when(config.getFieldConfig("E", "toG")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toH");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("H")));
        when(config.getFieldConfig("F", "toH")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toH");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("H")));
        when(config.getFieldConfig("G", "toH")).thenReturn(refField);

        refField = new ReferenceFieldConfig();
        refField.setName("toI");
        refField.setTypes(Collections.singletonList(new ReferenceFieldTypeConfig("I")));
        when(config.getFieldConfig("G", "toI")).thenReturn(refField);

        // ============== Другие поля ==============
        StringFieldConfig stringField = new StringFieldConfig();
        stringField.setName("bString");
        when(config.getFieldConfig("B", "bString")).thenReturn(stringField);

        stringField = new StringFieldConfig();
        stringField.setName("cString");
        when(config.getFieldConfig("C", "cString")).thenReturn(stringField);

        stringField = new StringFieldConfig();
        stringField.setName("fString");
        when(config.getFieldConfig("F", "fString")).thenReturn(stringField);

        stringField = new StringFieldConfig();
        stringField.setName("gString");
        when(config.getFieldConfig("G", "gString")).thenReturn(stringField);

        LongFieldConfig longField = new LongFieldConfig();
        longField.setName("fgLong");
        when(config.getFieldConfig("F", "fgLong")).thenReturn(longField);

        longField = new LongFieldConfig();
        longField.setName("fgLong");
        when(config.getFieldConfig("G", "fgLong")).thenReturn(longField);

        DateTimeFieldConfig dateField = new DateTimeFieldConfig();
        dateField.setName("hDate");
        when(config.getFieldConfig("H", "hDate")).thenReturn(dateField);

        dateField = new DateTimeFieldConfig();
        dateField.setName("iDate");
        when(config.getFieldConfig("I", "iDate")).thenReturn(dateField);
    }

    @Test
    public void testSimpleStringExpression() {
        DoelExpression expr = DoelExpression.parse("toC.toE.toG.gString");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "A");
        DoelValidator.ValidationResult result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть строковым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.STRING));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
        checkTypes(result, new String[] { "A", "C", "E", "G" });
    }

    @Test
    public void testSimpleObjectExpression() {
        DoelExpression expr = DoelExpression.parse("toB.toD.toForG");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "A");
        DoelValidator.ValidationResult result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть ссылочным",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.REFERENCE));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNotNull("Выражение должно возвращать доменный объект", result.getResultObjectTypes());
        assertTrue("Проверка типов возвращаемых доменных объектов", result.getResultObjectTypes().size() == 2 &&
                result.getResultObjectTypes().containsAll(Arrays.asList(new String[] { "F", "G" })));
        checkTypes(result, new String[] { "A", "B", "D" });
    }

    @Test
    public void testReverseLinkExpression() {
        DoelExpression expr = DoelExpression.parse("G^toI.D^toForG");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "I");
        DoelValidator.ValidationResult result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть ссылочным",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.REFERENCE));
        assertFalse("Выражение может возвращать множество значений", result.isSingleResult());
        assertNotNull("Выражение должно возвращать доменный объект", result.getResultObjectTypes());
        assertTrue("Проверка типа возвращаемого доменного объекта", result.getResultObjectTypes().size() == 1 &&
                result.getResultObjectTypes().contains("D"));
        checkTypes(result, new String[] { "I", "G" });
    }

    @Test
    public void testMultiTypeLinkExpression() {
        DoelExpression expr = DoelExpression.parse("toD.toForG.fgLong");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "B");
        DoelValidator.ValidationResult result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть числовым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.LONG));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
    }

    @Test
    public void testPartialCorrectExpression() {
        DoelExpression expr = DoelExpression.parse("toForG.toI.iDate");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "D");
        DoelValidator.ValidationResult result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect());
        assertFalse("Корректность выражения должна зависеть от значений полей", result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть датой",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.DATETIME));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
    }

    @Test
    public void testUnexistingFieldExpression() {
        DoelExpression expr = DoelExpression.parse("toB.toD.wrongField");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "A");
        DoelValidator.ValidationResult result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
        assertNull("Выражение не должно возвращать результат", result.getResultTypes());
        checkTypes(result, new String[] { "A", "B", "D" });
    }

    @Test
    public void testUnexistingLinkExpression() {
        DoelExpression expr = DoelExpression.parse("toC.toE.toB.toD");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "A");
        DoelValidator.ValidationResult result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
        assertNull("Выражение не должно возвращать результат", result.getResultTypes());
        checkTypes(result, new String[] { "A", "C", "E" });
    }

    @Test
    public void testInvalidLinkExpression() {
        DoelExpression expr = DoelExpression.parse("toC.cString.toE.toG");
        DoelValidator.ValidationProcessor proc = new DoelValidator.ValidationProcessor(expr, "A");
        DoelValidator.ValidationResult result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
        assertNull("Выражение не должно возвращать результат", result.getResultTypes());
        checkTypes(result, new String[] { "A", "C" });
    }

    private void checkTypes(DoelValidator.ValidationResult result, Object[] expectedTypes) {
        DoelValidator.Link link = result.getTypeChain();
        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals("Проверка типа объекта на шаге " + i, expectedTypes[i], link.getType());
            if (i < expectedTypes.length - 1) {
                assertNotNull("Цепочка типов должна содержать " + expectedTypes.length + " элементов", link.getNext());
                assertTrue("Цепочка типов должна содержать не должна содержать ветвлений", link.getNext().size() == 1);
                link = link.getNext().get(0);
            } else {
                assertTrue("Цепочка типов не должна быть длиннее заданной",
                        link.getNext() == null || link.getNext().size() == 0);
            }
        }
    }
}
