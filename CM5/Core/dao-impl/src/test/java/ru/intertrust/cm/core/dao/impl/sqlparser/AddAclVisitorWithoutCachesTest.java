package ru.intertrust.cm.core.dao.impl.sqlparser;

import org.junit.Before;
import org.junit.Test;

/**
 * Правильный с точки зрения unit тестов. Тесты не должны зависеть друг от друга.
 * Но тест с кэшем, как показала практика выявляет много скрытых проблем
 */
public class AddAclVisitorWithoutCachesTest extends AddAclVisitorWithCachesTest {

    @Before
    @Override
    public void setUp() {
        AddAclVisitor.clearCache();
        super.setUp();
    }

    @Test
    @Override
    public void testNoAclForNonTypeTable() {
        super.testNoAclForNonTypeTable();
    }

    @Test
    @Override
    public void testSingleType2() {
        super.testSingleType2();
    }

    @Test
    @Override
    public void testUsageOfParentType() {
        super.testUsageOfParentType();
    }

    @Test
    @Override
    public void testUsageOfLinkedType() {
        super.testUsageOfLinkedType();
    }

    @Test
    @Override
    public void testSingleType1_with_stamp() {
        super.testSingleType1_with_stamp();
    }

    @Test
    @Override
    public void testCaseInsensitiveness() {
        super.testCaseInsensitiveness();
    }

    @Test
    @Override
    public void testSubslectWithoutFrom() {
        super.testSubslectWithoutFrom();
    }

    @Test
    @Override
    public void testSelectWithWith() {
        super.testSelectWithWith();
    }

    @Test
    @Override
    public void testUsageOfLinkedTypeWithParent() {
        super.testUsageOfLinkedTypeWithParent();
    }

    @Test
    @Override
    public void testJoinOfIndependentTables() {
        super.testJoinOfIndependentTables();
    }

    @Test
    @Override
    public void testSubquery() {
        super.testSubquery();
    }

    @Test
    @Override
    public void testExists() {
        super.testExists();
    }

    @Test
    @Override
    public void testEliminateExcessiveAcl() {
        super.testEliminateExcessiveAcl();
    }

    @Test
    @Override
    public void testEliminateExcessiveAclMoreComplex() {
        super.testEliminateExcessiveAclMoreComplex();
    }

    @Test
    @Override
    public void testEliminateExcessiveAclEvenMoreComplex() {
        super.testEliminateExcessiveAclEvenMoreComplex();
    }

    @Test
    @Override
    public void testBasicQueryStartsWithWithRecursive() {
        super.testBasicQueryStartsWithWithRecursive();
    }

    @Test
    @Override
    public void testEliminateExcessiveAclInSubquery() {
        super.testEliminateExcessiveAclInSubquery();
    }
}
