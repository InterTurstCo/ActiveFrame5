package ru.intertrust.cm.core.business.api.dto.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.model.FatalException;

import static org.junit.Assert.assertEquals;

/**
 * @author vmatsukevich
 *         Date: 9/18/13
 *         Time: 11:29 AM
 */
public class RdbmsIdTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testSetFromStringRepresentation() throws Exception {
        String stringRepresentation = "0042000000001023";

        RdbmsId rdbmsId = new RdbmsId();
        rdbmsId.setFromStringRepresentation(stringRepresentation);

        assertEquals(42, rdbmsId.getTypeId());
        assertEquals(1023, rdbmsId.getId());
    }

    @Test
    public void testSetFromStringRepresentationNullString() throws Exception {
        expectedException.expect(NullPointerException.class);

        RdbmsId rdbmsId = new RdbmsId();
        rdbmsId.setFromStringRepresentation(null);
    }

    @Test
    public void testSetFromStringRepresentationInvalidLength() throws Exception {
        String stringRepresentation = "1234";

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid id string representation '" + stringRepresentation + "'. Must be " +
                "exactly " + RdbmsId.MAX_ID_LENGTH + " characters long");

        RdbmsId rdbmsId = new RdbmsId();
        rdbmsId.setFromStringRepresentation(stringRepresentation);
    }

    @Test
    public void testSetFromStringRepresentationWithLetters() throws Exception {
        String stringRepresentation = "00420000000a1023";

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(stringRepresentation + " can't be parsed");

        RdbmsId rdbmsId = new RdbmsId();
        rdbmsId.setFromStringRepresentation(stringRepresentation);
    }

    @Test
    public void testGenerateStringRepresentation() throws Exception {
        RdbmsId rdbmsId = new RdbmsId(42, 1023);
        String stringRepresentation = rdbmsId.generateStringRepresentation();

        assertEquals("0042000000001023", stringRepresentation);
    }

    @Test
    public void testGenerateStringRepresentationInvalidTypeIdLength() throws Exception {
        RdbmsId rdbmsId = new RdbmsId(42222, 1023);

        expectedException.expect(FatalException.class);
        expectedException.expectMessage("Domain Object type id '" + 42222  +"' exceeds " +
                RdbmsId.MAX_DO_TYPE_ID_LENGTH + " digits length.");

        rdbmsId.generateStringRepresentation();
    }

    @Test
    public void testGenerateStringRepresentationInvalidIdLength() throws Exception {
        RdbmsId rdbmsId = new RdbmsId(42, 1023111111111L);

        expectedException.expect(FatalException.class);
        expectedException.expectMessage("Domain Object id '" + 1023111111111L  +"' exceeds " +
                RdbmsId.MAX_DO_ID_LENGTH + " digits length.");

        rdbmsId.generateStringRepresentation();
    }
}
