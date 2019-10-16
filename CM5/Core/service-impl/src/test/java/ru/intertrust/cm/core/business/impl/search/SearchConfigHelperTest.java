package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class SearchConfigHelperTest {

    @InjectMocks
    private SearchConfigHelper testee = new SearchConfigHelper();

    @Before
    public void setup() throws Exception {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("config/search-test.xml");
        Reader reader = new InputStreamReader(resource, "UTF-8");
        char[] buffer = new char[32768];
        StringBuilder xml = new StringBuilder();
        int read;
        while ((read = reader.read(buffer)) != -1) {
            xml.append(buffer, 0, read);
        }
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer serializer = new ConfigurationSerializer();
        Configuration config = serializer.deserializeLoadedConfiguration(xml.toString());
        ConfigurationExplorer explorer = new ConfigurationExplorerImpl(config);

        initMocks(testee);
        testee.setConfigurationExplorer(explorer);

        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(ConfigurationExplorer.class)).thenReturn(explorer);
        new SpringApplicationContext().setApplicationContext(context);

        // Вручную вызываем метод, помеченный аннотацией @PostConstruct
        Method init = testee.getClass().getDeclaredMethod("initGlobalConfigs", new Class<?>[0]);
        init.setAccessible(true);
        init.invoke(testee, new Object[0]);
    }

    @Test
    public void testFindIndexedFieldConfigs() {
        List<IndexedFieldConfig> configs = testee.findIndexedFieldConfigs("String_D", "Area_A");
        assertTrue("Поле String_D присутствует в конфигурации области Area_A 2 раза, а найдено " + configs.size(),
                configs.size() == 2);
    }

    @Test
    public void testFindIndexedFieldConfig_OK() {
        IndexedFieldConfig config = testee.findIndexedFieldConfig("String_B", "Area_A", "Type_A");
        assertNotNull("Поле String_B имеется в конфигурации объекта Type_A области Area_A", config);
    }

    @Test
    public void testFindIndexedFieldConfig_None() {
        IndexedFieldConfig config = testee.findIndexedFieldConfig("String_B", "Area_A", "Type_D");
        assertNull("Поле String_B отсутствует в конфигурации объекта Type_D области Area_A", config);
    }

    @Test
    public void testFindEffectiveConfigs_Once() {
        List<SearchConfigHelper.SearchAreaDetailsConfig> configs = testee.findEffectiveConfigs("Type_A");
        assertTrue("Объект Type_A должен индексироваться 1 раз, а не " + configs.size(), configs.size() == 1);
        assertTrue("Объект Type_A должен индексироваться в Area_A, а не в " + configs.get(0).getAreaName(),
                "Area_A".equalsIgnoreCase(configs.get(0).getAreaName()));
        assertTrue("Объект Type_A должен индексироваться для поиска по Type_A, а не в "
                + configs.get(0).getTargetObjectType(),
                "Type_A".equalsIgnoreCase(configs.get(0).getTargetObjectType()));
    }

    @Test
    public void testFindEffectiveConfigs_TwiceDifferentAreas() {
        List<SearchConfigHelper.SearchAreaDetailsConfig> configs = testee.findEffectiveConfigs("Type_B");
        assertTrue("Объект Type_B должен индексироваться 3 раза, а не " + configs.size(), configs.size() == 3);
        assertTrue("Объект Type_B должен индексироваться в Area_A, а не в " + configs.get(0).getAreaName(),
                "Area_A".equalsIgnoreCase(configs.get(0).getAreaName()));
        assertTrue("Объект Type_B должен индексироваться для поиска по Type_A, а не в "
                + configs.get(0).getTargetObjectType(),
                "Type_A".equalsIgnoreCase(configs.get(0).getTargetObjectType()));
        assertTrue("Объект Type_B должен индексироваться в Area_B, а не в " + configs.get(1).getAreaName(),
                "Area_B".equalsIgnoreCase(configs.get(1).getAreaName()));
        assertTrue("Объект Type_B должен индексироваться для поиска по Type_B, а не в "
                + configs.get(1).getTargetObjectType(),
                "Type_B".equalsIgnoreCase(configs.get(1).getTargetObjectType()));
    }

    @Test
    public void testFindEffectiveConfigs_TwiceSameArea() {
        List<SearchConfigHelper.SearchAreaDetailsConfig> configs = testee.findEffectiveConfigs("Type_D");
        assertTrue("Объект Type_D должен индексироваться 2 раза, а не " + configs.size(), configs.size() == 2);
        assertTrue("Объект Type_D должен индексироваться в Area_A, а не в " + configs.get(0).getAreaName(),
                "Area_A".equalsIgnoreCase(configs.get(0).getAreaName()));
        assertTrue("Объект Type_D должен индексироваться для поиска по Type_A, а не в "
                + configs.get(0).getTargetObjectType(),
                "Type_A".equalsIgnoreCase(configs.get(0).getTargetObjectType()));
        assertTrue("Объект Type_D должен индексироваться в Area_A, а не в " + configs.get(1).getAreaName(),
                "Area_A".equalsIgnoreCase(configs.get(1).getAreaName()));
        assertTrue("Объект Type_D должен индексироваться для поиска по Type_D, а не в "
                + configs.get(1).getTargetObjectType(),
                "Type_D".equalsIgnoreCase(configs.get(1).getTargetObjectType()));
    }

    @Test
    public void testFindEffectiveConfigs_Attachment() {
        List<SearchConfigHelper.SearchAreaDetailsConfig> configs = testee.findEffectiveConfigs("Attach_C");
        assertTrue("Объект Attach_C должен индексироваться 1 раз, а не " + configs.size(), configs.size() == 1);
        assertTrue("Объект Attach_C должен индексироваться в Area_A, а не в " + configs.get(0).getAreaName(),
                "Area_A".equalsIgnoreCase(configs.get(0).getAreaName()));
        assertTrue("Объект Attach_C должен индексироваться для поиска по Type_D, а не по "
                + configs.get(0).getTargetObjectType(),
                "Type_D".equalsIgnoreCase(configs.get(0).getTargetObjectType()));
    }

    @Test
    public void testIsSuitableType_Same() {
        boolean result = testee.isSuitableType("Type_A", "Type_A");
        assertTrue(result);
    }

    @Test
    public void testIsSuitableType_Successor() {
        boolean result = testee.isSuitableType("Type_D", "Type_Da");
        assertTrue(result);
    }

    @Test
    public void testIsSuitableType_Independant() {
        boolean result = testee.isSuitableType("Type_A", "Type_Ca");
        assertFalse(result);
    }

    @Test
    public void testGetFieldType_SimpleString() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("String_B");
        when(config.getDoel()).thenReturn(null);
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_B");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new TextSearchFieldType(Arrays.asList("ru", "en"), false, false));
    }

    @Test
    public void testGetFieldType_SimpleLong() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("Long_C");
        when(config.getDoel()).thenReturn(null);
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_C");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG));
    }

    @Test
    public void testGetFieldType_DoelDate() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("DateTime_A");
        when(config.getDoel()).thenReturn("Reference_B_A.DateTime_A");
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_B");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE));
    }

    @Test
    public void testGetFieldType_DoelMultipleDate() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("DateTimeZone_B");
        when(config.getDoel()).thenReturn("Reference_D_A.Type_B^Reference_B_A.DateTimeZone_B");
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_D");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE, true));
    }

    @Test
    public void testGetFieldType_DoelChildType() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("String_Ca");
        when(config.getDoel()).thenReturn("String_Ca");
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_C");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new TextSearchFieldType(Arrays.asList("ru", "en"), false, false));
    }

    @Test
    public void testGetFieldType_Calculated() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("Calculated");
        when(config.getDoel()).thenReturn(null);
        when(config.getScript()).thenReturn("ctx.get('String_B') + ctx.get('String_Bc') + ctx.get('String_Bd')");
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_B");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new TextSearchFieldType(Arrays.asList("ru", "en"), false, false));
    }

    @Test
    public void testGetFieldType_Substring() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("String_Bc");
        when(config.getSearchBy()).thenReturn(IndexedFieldConfig.SearchBy.SUBSTRING);
        when(config.getDoel()).thenReturn(null);
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_B");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new TextSearchFieldType(Arrays.asList("ru", "en"), false, true));
    }

    @Test
    public void testGetFieldType_CustomType() {
        IndexedFieldConfig config = mock(IndexedFieldConfig.class);
        when(config.getName()).thenReturn("String_Bc");
        when(config.getSolrPrefix()).thenReturn("spec");
        when(config.getDoel()).thenReturn(null);
        Set<SearchFieldType> types = testee.getFieldTypes(config, "Type_B");
        assertEquals(types.size(), 1);
        assertEquals(types.iterator().next(), new CustomSearchFieldType("spec_"));
    }

    @Test
    public void testGetFieldTypes_Once() {
        // Поле встречается один раз в одной области поиска
        HashSet<String> areas = new HashSet<>(Arrays.asList("Area_A", "Area_B"));
        Set<SearchFieldType> types = testee.getFieldTypes("Long_C", areas);
        assertTrue("Поле Long_C должно иметь тип LONG, а не " + types,
                types.size() == 1 && types.contains(new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG)));
    }

    @Test
    public void testGetFieldTypes_TwiceSameArea() {
        // Поле встречается 2 раза в одной области поиска
        HashSet<String> areas = new HashSet<>(Arrays.asList("Area_A", "Area_B"));
        Set<SearchFieldType> types = testee.getFieldTypes("DateTime_D", areas);
        assertTrue("Поле Long_C должно иметь тип DATE, а не " + types,
                types.size() == 1 && types.contains(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE)));
    }

    @Test
    public void testGetFieldTypes_TwiceDifferentAreas() {
        // Поле встречается 2 раза в разных областях поиска
        HashSet<String> areas = new HashSet<>(Arrays.asList("Area_A", "Area_B"));
        Set<SearchFieldType> types = testee.getFieldTypes("String_A", areas);
        assertTrue("Поле Long_C должно иметь тип STRING, а не " + types,
                types.size() == 1 && types.contains(new TextSearchFieldType(Arrays.asList("ru", "en"), false, false)));
    }

    @Test
    public void testGetFieldType_TwiceDifferentTypes() {
        // Поле встречается 2 раза в разных областях поиска и имеет разные типы
        HashSet<String> areas = new HashSet<>(Arrays.asList("Area_A", "Area_B"));
        Set<SearchFieldType> types = testee.getFieldTypes("DiffType", areas);
        assertTrue("Поле Long_C должно иметь типы STRING и DATE, а не " + types, types.size() == 2
                && types.contains(new TextSearchFieldType(Arrays.asList("ru", "en"), false, false))
                && types.contains(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE)));
    }

    @Test
    public void testGetFieldTypes_None() {
        // Поле отсутствует в заданной области поиска (но присутствует в другой)
        Set<SearchFieldType> types = testee.getFieldTypes("DateTime_C", Collections.singleton("Area_B"));
        assertTrue("Поле DateTime_C отсутствует в конфигурации области Area_B", types.size() == 0);
    }

    @Test
    public void testIsAttachmentObject_Yes() {
        DomainObject object = mock(DomainObject.class);
        when(object.getTypeName()).thenReturn("Attach_A");
        assertTrue("Attach_A должен определяться как объект вложения", testee.isAttachmentObject(object));
    }

    @Test
    public void testIsAttachmentObject_No() {
        DomainObject object = mock(DomainObject.class);
        when(object.getTypeName()).thenReturn("Type_D");
        assertFalse("Type_D не должен определяться как объект вложения", testee.isAttachmentObject(object));
    }

    @Test
    public void testGetAttachmentParentLinkName_Simple() {
        String linkName = testee.getAttachmentParentLinkName("Attach_A", "Type_A");
        assertTrue("Поле для связи с родительским объектом во вложении Attach_A, индексируемом внутри объекта Type_A, "
                + "должно иметь имя Type_A, а не " + linkName,
                "Type_A".equalsIgnoreCase(linkName));
    }

    @Test
    public void testGetAttachmentParentLinkName_Inherited() {
        String linkName = testee.getAttachmentParentLinkName("Attach_C", "Type_Ca");
        assertTrue("Поле для связи с родительским объектом во вложении Attach_C, индексируемом внутри объекта Type_Ca, "
                + "должно иметь имя Type_C, а не " + linkName,
                "Type_C".equalsIgnoreCase(linkName));
    }

    @Test
    public void testGetSupportedLanguagesGlobal() {
        List<String> langs = testee.getSupportedLanguages();
        assertTrue("Должно быть определено 2 языка", langs.size() == 2);
        assertTrue("Среди языков должен быть русский (ru)", langs.contains("ru"));
        assertTrue("Среди языков должен быть английский (en)", langs.contains("en"));
    }

    @Test
    public void testGetSupportedLanguagesPerField_NotSpecified() {
        List<String> langs = testee.getSupportedLanguages("String_A", "Area_A");
        assertTrue("Для поля String_A должно быть определено 2 языка", langs.size() == 2);
        assertTrue("Для поля String_A среди языков должен быть русский (ru)", langs.contains("ru"));
        assertTrue("Для поля String_A среди языков должен быть английский (en)", langs.contains("en"));
    }

    @Test
    public void testGetSupportedLanguagesPerField_Specified() {
        List<String> langs = testee.getSupportedLanguages("String_D", "Area_A");
        assertTrue("Для поля String_D должен быть определен 1 язык", langs.size() == 1);
        assertTrue("Для поля String_D среди языков должен быть русский (ru)", langs.contains("ru"));
    }

    @Test
    public void testGetSupportedLanguagesPerField_SpecifiedNone() {
        List<String> langs = testee.getSupportedLanguages("String_C", "Area_A");
        assertTrue("Для поля String_C должен быть определён нетранслируемый язык",
                langs.size() == 1 && langs.contains(""));
    }
}
