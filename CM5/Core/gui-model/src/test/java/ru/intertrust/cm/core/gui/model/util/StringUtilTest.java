package ru.intertrust.cm.core.gui.model.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Набор тестов для класса утилит работы со строками {@link ru.intertrust.cm.core.gui.model.util.StringUtil}
 * <br><br>
 * <p>
 * Created by Myskin Sergey on 21.05.2019.
 */
public class StringUtilTest {

    private final String ID_STR_1 = "5029000000000001";
    private final String ID_STR_2 = "5029000000000002";

    private final String HISTORY_TEST_LINK_1 = "Departments-" + ID_STR_1 + ".Employees";
    private final String HISTORY_TEST_LINK_2 = "Departments-" + ID_STR_1 + ".Employees-" + ID_STR_2 + "-asd";
    private final String HISTORY_TEST_LINK_3 = "Departments-" + ID_STR_1 + "-123";
    private final String HISTORY_TEST_LINK_4 = "Departments-" + ID_STR_1 + "-123" + "." + ID_STR_2;
    private final String HISTORY_TEST_LINK_5 = "Departments-" + ID_STR_1 + "-123" + "." + ID_STR_2 + ".321";

    /**
     * Тест работы метода {@link ru.intertrust.cm.core.gui.model.util.StringUtil#isNullOrBlank(String)}
     */
    @Test
    public void testIsNullOrBlankMethod() {
        Assert.assertTrue(StringUtil.isNullOrBlank(null));
        Assert.assertTrue(StringUtil.isNullOrBlank(""));
        Assert.assertTrue(StringUtil.isNullOrBlank(" "));
        Assert.assertTrue(StringUtil.isNullOrBlank("    "));
        Assert.assertFalse(StringUtil.isNullOrBlank("abc"));
        Assert.assertFalse(StringUtil.isNullOrBlank("  abc  "));
    }

    /**
     * Тест работы метода {@link ru.intertrust.cm.core.gui.model.util.StringUtil#getLastIdStrFromHistoryLink(String)}
     */
    @Test
    public void getLastIdStrFromHistoryLinkTest() {
        Assert.assertEquals(ID_STR_1, StringUtil.getLastIdStrFromHistoryLink(HISTORY_TEST_LINK_1));
        Assert.assertEquals(ID_STR_2, StringUtil.getLastIdStrFromHistoryLink(HISTORY_TEST_LINK_2));
        Assert.assertEquals(ID_STR_1, StringUtil.getLastIdStrFromHistoryLink(HISTORY_TEST_LINK_3));
        Assert.assertEquals(ID_STR_2, StringUtil.getLastIdStrFromHistoryLink(HISTORY_TEST_LINK_4));
        Assert.assertEquals(ID_STR_2, StringUtil.getLastIdStrFromHistoryLink(HISTORY_TEST_LINK_5));
    }

}
