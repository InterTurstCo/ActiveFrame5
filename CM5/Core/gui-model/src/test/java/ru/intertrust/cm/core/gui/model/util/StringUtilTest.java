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

}
