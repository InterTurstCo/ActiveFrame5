package ru.intertrust.cm.core.config.doel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.LongFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    private DoelFunctionRegistry functionRegistry;

    @Mock
    private ApplicationContext context;

    /* Схема тестовых типов, полей и связей:
     *   -> B -> D *  -> F -> H
     * A         v   |     /
     *   -> C -> E --+-> G -> I
     *       Ca -----
     * A: toB toC
     * B: toD bString
     * C: toE cString
     * Ca:toF caDate (extends C)
     * D: toE toAny
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
        when(context.getBean(DoelFunctionRegistry.class)).thenReturn(functionRegistry);

        // ================ Связи =================
        when(config.getFieldConfig("A", "toB")).thenReturn(referenceFieldConfig("toB", "B"));
        when(config.getFieldConfig("A", "toC")).thenReturn(referenceFieldConfig("toC", "C"));
        when(config.getFieldConfig("B", "toD")).thenReturn(referenceFieldConfig("toD", "D"));
        when(config.getFieldConfig("C", "toE")).thenReturn(referenceFieldConfig("toE", "E"));
        when(config.getFieldConfig("Ca", "toF")).thenReturn(referenceFieldConfig("toF", "F"));
        when(config.getFieldConfig("D", "toE")).thenReturn(referenceFieldConfig("toE", "E"));
        when(config.getFieldConfig("D", "toAny")).thenReturn(referenceFieldConfig("toAny", "*"));
        when(config.getFieldConfig("E", "toG")).thenReturn(referenceFieldConfig("toG", "G"));
        when(config.getFieldConfig("F", "toH")).thenReturn(referenceFieldConfig("toH", "H"));
        when(config.getFieldConfig("G", "toH")).thenReturn(referenceFieldConfig("toH", "H"));
        when(config.getFieldConfig("G", "toI")).thenReturn(referenceFieldConfig("toI", "I"));

        // ============== Другие поля ==============
        when(config.getFieldConfig("B", "bString")).thenReturn(fieldConfig(StringFieldConfig.class, "bString"));
        when(config.getFieldConfig("C", "cString")).thenReturn(fieldConfig(StringFieldConfig.class, "cString"));
        when(config.getFieldConfig("Ca", "caDate")).thenReturn(fieldConfig(DateTimeFieldConfig.class, "caDate"));
        when(config.getFieldConfig("F", "fString")).thenReturn(fieldConfig(StringFieldConfig.class, "fString"));
        when(config.getFieldConfig("G", "gString")).thenReturn(fieldConfig(StringFieldConfig.class, "gString"));
        when(config.getFieldConfig("F", "fgLong")).thenReturn(fieldConfig(LongFieldConfig.class, "fgLong"));
        when(config.getFieldConfig("G", "fgLong")).thenReturn(fieldConfig(LongFieldConfig.class, "fgLong"));
        when(config.getFieldConfig("H", "hDate")).thenReturn(fieldConfig(DateTimeFieldConfig.class, "hDate"));
        when(config.getFieldConfig("I", "iDate")).thenReturn(fieldConfig(DateTimeFieldConfig.class, "iDate"));

        // ============= Доменные объекты =============
        DomainObjectTypeConfig caConfigMock;
        ArrayList<DomainObjectTypeConfig> types = new ArrayList<>();
        types.add(typeConfigMock("A"));
        types.add(typeConfigMock("B"));
        types.add(typeConfigMock("C"));
        types.add(caConfigMock = typeConfigMock("Ca", "C"));
        types.add(typeConfigMock("D"));
        types.add(typeConfigMock("E"));
        types.add(typeConfigMock("F"));
        types.add(typeConfigMock("G"));
        types.add(typeConfigMock("H"));
        types.add(typeConfigMock("I"));
        when(config.getConfigs(DomainObjectTypeConfig.class)).thenReturn(types);

        when(config.getDomainObjectRootType(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });
        when(config.getDomainObjectRootType("Ca")).thenReturn("C");
        when(config.findChildDomainObjectTypes("C", false)).thenReturn(Collections.singletonList(caConfigMock));

        // ============= Функции =============
        when(functionRegistry.getFunctionValidator("univ")).thenReturn(new AnnotationFunctionValidator(
                UniversalFunction.class.getAnnotation(DoelFunction.class)));
        when(functionRegistry.getFunctionValidator("totext")).thenReturn(new AnnotationFunctionValidator(
                ToTextFunction.class.getAnnotation(DoelFunction.class)));
        when(functionRegistry.getFunctionValidator("ref")).thenReturn(new AnnotationFunctionValidator(
                ReferenceProcessingFunction.class.getAnnotation(DoelFunction.class)));
        when(functionRegistry.getFunctionValidator("params")).thenReturn(new AnnotationFunctionValidator(
                ParametrizedFunction.class.getAnnotation(DoelFunction.class)));
        when(functionRegistry.getFunctionValidator("single")).thenReturn(new AnnotationFunctionValidator(
                SingleChoosingFunction.class.getAnnotation(DoelFunction.class)));
    }

    private ReferenceFieldConfig referenceFieldConfig(String name, String type) {
        ReferenceFieldConfig config = new ReferenceFieldConfig();
        config.setName(name);
        config.setType(type);
        return config;
    }

    private <T extends FieldConfig> T fieldConfig(Class<T> type, String name) {
        T config;
        try {
            config = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        config.setName(name);
        return config;
    }

    private DomainObjectTypeConfig typeConfigMock(String name) {
        return typeConfigMock(name, null);
    }

    private DomainObjectTypeConfig typeConfigMock(String name, String parentType) {
        DomainObjectTypeConfig mock = mock(DomainObjectTypeConfig.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getExtendsAttribute()).thenReturn(parentType);
        return mock;
    }

    @Test
    public void testSimpleStringExpression() {
        DoelExpression expr = DoelExpression.parse("toC.toE.toG.gString");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть строковым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.STRING));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
        checkTypes(result, new String[] { "A", "C", "E", "G" });
    }

    @Test
    public void testSimpleObjectExpression() {
        DoelExpression expr = DoelExpression.parse("toB.toD.toAny");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть ссылочным",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.REFERENCE));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNotNull("Выражение должно возвращать доменный объект", result.getResultObjectTypes());
        assertTrue("Проверка типов возвращаемых доменных объектов", result.getResultObjectTypes().size() == 1 &&
                result.getResultObjectTypes().containsAll(Arrays.asList(new String[] { "*" })));
        checkTypes(result, new String[] { "A", "B", "D" });
    }

    @Test
    public void testReverseLinkExpression() {
        DoelExpression expr = DoelExpression.parse("G^toH.D^toAny");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "H");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть ссылочным",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.REFERENCE));
        assertFalse("Выражение может возвращать множество значений", result.isSingleResult());
        assertNotNull("Выражение должно возвращать доменный объект", result.getResultObjectTypes());
        assertTrue("Проверка типа возвращаемого доменного объекта", result.getResultObjectTypes().size() == 1 &&
                result.getResultObjectTypes().contains("D"));
        checkTypes(result, new String[] { "H", "G" });
    }

    @Test
    public void testMultiTypeLinkExpression() {
        DoelExpression expr = DoelExpression.parse("toD.toAny.fgLong");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "B");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect());
        assertFalse("Выражение должно быть частично корректным", result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть числовым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.LONG));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
    }

    @Test
    public void testPartialCorrectExpression() {
        DoelExpression expr = DoelExpression.parse("toAny.toI.iDate");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "D");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть всегда корректным", result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть датой",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.DATETIME));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
    }

    @Test
    public void testReverseLinkAfterMultiTypeLinkExpression() {
        DoelExpression expr = DoelExpression.parse("toAny.A^toC");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "D");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть всегда корректным", result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть ссылочным",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.REFERENCE));
        assertFalse("Выражение может возвращать множество значений", result.isSingleResult());
        assertTrue("Проверка типа возвращаемого доменного объекта", result.getResultObjectTypes().size() == 1 &&
                result.getResultObjectTypes().contains("A"));
    }

    @Test
    public void testDescendantTypeFieldExpression() {
        DoelExpression expr = DoelExpression.parse("toC.toF.fString");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть всегда корректным", result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть строковым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.STRING));
        assertTrue("Выражение должно возвращать единственный результат", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
        checkTypes(result, new String[] { "A", "Ca", "F" });
        assertNull("Выражение не должно возвращать доменные объекты", result.getResultObjectTypes());
    }

    @Test
    public void testUnexistingFieldExpression() {
        DoelExpression expr = DoelExpression.parse("toB.toD.wrongField");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
        assertNull("Выражение не должно возвращать результат", result.getResultTypes());
        assertNull("Не должно существовать правильных цепочек типов", result.getTypeChain());
    }

    @Test
    public void testUnexistingLinkExpression() {
        DoelExpression expr = DoelExpression.parse("toC.toE.toB.toD");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
        assertNull("Выражение не должно возвращать результат", result.getResultTypes());
        //checkTypes(result, new String[] { "A", "C", "E" });
    }

    @Test
    public void testInvalidLinkExpression() {
        DoelExpression expr = DoelExpression.parse("toC.cString.toE.toG");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
        assertNull("Выражение не должно возвращать результат", result.getResultTypes());
        //checkTypes(result, new String[] { "A", "C" });
    }

    @DoelFunction(name = "univ")
    public static class UniversalFunction { }

    @DoelFunction(name = "totext", changesType = true, resultType = FieldType.STRING)
    public static class ToTextFunction { }

    @DoelFunction(name = "ref", contextTypes = { FieldType.REFERENCE })
    public static class ReferenceProcessingFunction { }

    @DoelFunction(name = "params", requiredParams = 1, optionalParams = 3)
    public static class ParametrizedFunction { }

    @DoelFunction(name = "single", resultMultiple = false)
    public static class SingleChoosingFunction { }

    @Test
    public void testNonConvertingFunctionAtLink() {
        DoelExpression expr = DoelExpression.parse("B^toD:univ.A^toB");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "D");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть ссылочным",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.REFERENCE));
        assertFalse("Выражение может возвращать множество значений", result.isSingleResult());
        assertNotNull("Выражение должно возвращать доменный объект", result.getResultObjectTypes());
        assertTrue("Проверка типа возвращаемого доменного объекта", result.getResultObjectTypes().size() == 1 &&
                result.getResultObjectTypes().contains("A"));
        checkTypes(result, new String[] { "D", "B" });
    }

    @Test
    public void testNonConvertingFunctionAtEnd() {
        DoelExpression expr = DoelExpression.parse("F^toH.fString:univ");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "H");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть строковым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.STRING));
        assertFalse("Выражение может возвращать множество значений", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменный объект", result.getResultObjectTypes());
        checkTypes(result, new String[] { "H", "F" });
    }

    @Test
    public void testMultipleFunctions() {
        DoelExpression expr = DoelExpression.parse("toE:params(a,b):univ.toG.gString");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "C");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть строковым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.STRING));
        assertTrue("Выражение должно возвращать единственное значение", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменный объект", result.getResultObjectTypes());
        checkTypes(result, new String[] { "C", "E", "G" });
    }

    @Test
    public void testConvertingFunctionAtEnd() {
        DoelExpression expr = DoelExpression.parse("toI.iDate:totext");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "G");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть строковым",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.STRING));
        assertTrue("Выражение должно возвращать единственное значение", result.isSingleResult());
        assertNull("Выражение не должно возвращать доменный объект", result.getResultObjectTypes());
        checkTypes(result, new String[] { "G", "I" });
    }

    @Test
    public void testStickingFunction() {
        DoelExpression expr = DoelExpression.parse("Ca^toF.A^toC:single");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "F");
        DoelValidator.DoelTypes result = proc.process();
        assertTrue("Выражение должно быть корректным", result.isCorrect() && result.isAlwaysCorrect());
        assertTrue("Тип результата выражения должен быть ссылочным",
                result.getResultTypes().size() == 1 && result.getResultTypes().contains(FieldType.REFERENCE));
        assertTrue("Выражение должно возвращать единственное значение", result.isSingleResult());
        assertNotNull("Выражение должно возвращать доменный объект", result.getResultObjectTypes());
        assertTrue("Проверка типа возвращаемого доменного объекта", result.getResultObjectTypes().size() == 1 &&
                result.getResultObjectTypes().contains("A"));
        checkTypes(result, new String[] { "F", "Ca" });
    }

    @Test
    public void testFunctionWithIllegalParameters() {
        DoelExpression expr = DoelExpression.parse("toC.toE:params");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
    }

    @Test
    public void testFunctionInIllegalContext() {
        DoelExpression expr = DoelExpression.parse("toB.bString:ref");
        DoelValidator.Processor proc = new DoelValidator.Processor(expr, "A");
        DoelValidator.DoelTypes result = proc.process();
        assertFalse("Выражение не должно быть корректным", result.isCorrect());
        assertFalse("Выражение не должно быть корректным", result.isAlwaysCorrect());
    }

    private void checkTypes(DoelValidator.DoelTypes result, Object[] expectedTypes) {
        DoelValidator.DoelTypes.Link link = result.getTypeChain();
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
