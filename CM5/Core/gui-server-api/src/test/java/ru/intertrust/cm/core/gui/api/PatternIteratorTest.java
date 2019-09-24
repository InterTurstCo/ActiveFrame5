package ru.intertrust.cm.core.gui.api;

import org.junit.Assert;
import org.junit.Test;
import ru.intertrust.cm.core.gui.impl.server.widget.PatternIterator;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.02.2015
 *         Time: 7:53
 */
public class PatternIteratorTest {
    @Test
    public void testDirectReference(){
        PatternIterator patternIterator = new PatternIterator("country.city");
        patternIterator.moveToNext();
        Assert.assertEquals(PatternIterator.ReferenceType.DIRECT_REFERENCE, patternIterator.getType());
        Assert.assertEquals("country", patternIterator.getValue());
    }
    @Test
    public void testOneToOneBackReference(){
        PatternIterator patternIterator = new PatternIterator("city|country");
        patternIterator.moveToNext();
        Assert.assertEquals(PatternIterator.ReferenceType.BACK_REFERENCE_ONE_TO_ONE, patternIterator.getType());
        Assert.assertEquals("city", patternIterator.getValue());
    }
    @Test
    public void testTraversing(){
        PatternIterator patternIterator = new PatternIterator("city|country.union.name");
        patternIterator.moveToNext();
        patternIterator.moveToNext();
        Assert.assertEquals("city|country", patternIterator.getTraversed());
        Assert.assertEquals("union.name", patternIterator.getNotTraversed());
    }
}
