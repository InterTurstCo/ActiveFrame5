package ru.intertrust.cm.core.dao.impl.sqlparser;

import org.junit.Test;

public class BasicVisitorTest {
    private BasicVisitor basicVisitor = new BasicVisitor();

    @Test
    public void testSafeTraversal() {
        SqlQueryParser p = new SqlQueryParser("select id, created date, a, b, c from (select bd.id, "
                + "bd.created_date, bd.area, (select string_agg(a, ', ') from attribute a where a.document = d.id) a"
                + ", case when d.b is null then 'undefined' else d.b end b"
                + ", case when (select sum(1) from (select 1 f from c1 where c1.document = d.id union select 1 f from c2 where c2.document = d.id) tt) > 0 "
                + "then 1 else 0 end c from document d join base_document bd on bd.id = d.id where bd.condition = 1 and d.idx != null) t"
                + " join area a on a.id = bd.area where created_date between yesterday() and now() and a like '%x%' and a.name ~ '75'");
        basicVisitor.visit(p.getSelectStatement());
    }

    @Test
    public void testSelectWihoutFrom() {
        SqlQueryParser p = new SqlQueryParser("select 1");
        basicVisitor.visit(p.getSelectStatement());
    }

    @Test
    public void testValuesList() {
        SqlQueryParser p = new SqlQueryParser(
                "with t as (select column1, column2 from (values (1, 2), (2, 3), (3, 2)) tt) select column1 from a join t on t.column2 = a.id");
        basicVisitor.visit(p.getSelectStatement());
    }
}
