package ru.intertrust.cm.core.dao.impl.sqlparser;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.sf.jsqlparser.statement.select.PlainSelect;

import org.junit.Test;

import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer.TypeConfigBuilder;

public class CollectingSelectItemConfigVisitorTest {

    private FakeConfigurationExplorer configurationExplorer = new FakeConfigurationExplorer();

    @Test
    public void testPlain() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("document")).addStringField("subject"));
        PlainSelect plainSelect = plainSelect("select id, created_date, subject from document d");
        CollectingSelectItemConfigVisitor visitor = new CollectingSelectItemConfigVisitor(configurationExplorer, plainSelect);
        plainSelect.accept(visitor);
        assertEquals(singletonMap("subject", configurationExplorer.getFieldConfig("document", "subject")), visitor.getColumnToConfigMapping());
    }

    @Test
    public void testAliasMatchesTheNameOfUnrelatedField() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("document")).addStringField("subject"));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("document_addressee")).addStringField("name").addReferenceField("owner", "document"));
        PlainSelect plainSelect = plainSelect("select addressee, owner from (select name addressee, subject owner from document d "
                + "join document_addressee da on da.owner = d.id where d.subject = 'x') t");
        CollectingSelectItemConfigVisitor visitor = new CollectingSelectItemConfigVisitor(configurationExplorer, plainSelect);
        plainSelect.accept(visitor);
        assertFalse(visitor.getColumnToConfigMapping().get("owner") instanceof ReferenceFieldConfig);
    }

    @Test
    public void testNotRefField() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("document")).addStringField("subject").addReferenceField("owner", "person"));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("document_addressee")).addStringField("name").addReferenceField("owner", "document"));
        PlainSelect plainSelect = plainSelect("select 'xxx' as owner from (select owner from document) t");
        CollectingSelectItemConfigVisitor visitor = new CollectingSelectItemConfigVisitor(configurationExplorer, plainSelect);
        plainSelect.accept(visitor);
        assertTrue(visitor.getColumnToConfigMapping().get("owner") == null);
    }
    
    protected PlainSelect plainSelect(String query) {
        PlainSelect plainSelect = (PlainSelect) (new SqlQueryParser(query)).getSelectStatement().getSelectBody();
        return plainSelect;
    }

}
