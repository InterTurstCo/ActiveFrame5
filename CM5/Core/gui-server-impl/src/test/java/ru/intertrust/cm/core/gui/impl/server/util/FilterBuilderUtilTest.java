package ru.intertrust.cm.core.gui.impl.server.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Myskin Sergey on 23.08.2019.
 */
public class FilterBuilderUtilTest {

    @Test
    public void cutPercentsCharactersTest() {
        final String STR_1 = "%value%";
        final String result1 = FilterBuilderUtil.cutPercentsCharacters(STR_1);
        Assert.assertEquals("value", result1);

        final String STR_2 = "%%value%";
        final String result2 = FilterBuilderUtil.cutPercentsCharacters(STR_2);
        Assert.assertEquals("value", result2);

        final String STR_3 = "%%value%%";
        final String result3 = FilterBuilderUtil.cutPercentsCharacters(STR_3);
        Assert.assertEquals("value", result3);

        final String STR_4 = "value";
        final String result4 = FilterBuilderUtil.cutPercentsCharacters(STR_4);
        Assert.assertEquals("value", result4);

        final String STR_5 = "%val%ue%";
        final String result5 = FilterBuilderUtil.cutPercentsCharacters(STR_5);
        Assert.assertEquals("value", result5);

        final String STR_6 = "val%ue";
        final String result6 = FilterBuilderUtil.cutPercentsCharacters(STR_6);
        Assert.assertEquals("value", result6);
    }

}
