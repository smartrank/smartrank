package nl.minvenj.nfi.smartrank.report.jasper.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JasperFieldTest {

    @Test
    public final void testJasperFieldStringStringClassOfQ() {
        final JasperField jasperField = new JasperField("Name", "Description", String.class);
        assertEquals(-1, jasperField.getColumnIndex());
        assertEquals("Name", jasperField.getName());
        assertEquals("Description", jasperField.getDescription());
        assertEquals(String.class, jasperField.getValueClass());
    }

    @Test
    public final void testJasperFieldStringNullClassOfQ() {
        final JasperField jasperField = new JasperField("Name", null, String.class);
        assertEquals(-1, jasperField.getColumnIndex());
        assertEquals("Name", jasperField.getName());
        assertEquals("", jasperField.getDescription());
        assertEquals(String.class, jasperField.getValueClass());
    }

    @Test
    public final void testJasperFieldStringStringIntClassOfQ() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        assertEquals(123, jasperField.getColumnIndex());
        assertEquals("Name", jasperField.getName());
        assertEquals("Description", jasperField.getDescription());
        assertEquals(String.class, jasperField.getValueClass());
    }

    @Test
    public final void testClone() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        final Object jasperFieldClone = jasperField.clone();
        assertTrue(jasperFieldClone instanceof JasperField);
        assertEquals(jasperField.getName(), ((JasperField) jasperFieldClone).getName());
        assertEquals(jasperField.getDescription(), ((JasperField) jasperFieldClone).getDescription());
        assertEquals(jasperField.getColumnIndex(), ((JasperField) jasperFieldClone).getColumnIndex());
        assertEquals(jasperField.getValueClass(), ((JasperField) jasperFieldClone).getValueClass());
    }

    @Test
    public final void testDescription() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        assertEquals("Description", jasperField.getDescription());
        jasperField.setDescription("Bla");
        assertEquals("Bla", jasperField.getDescription());
    }

    @Test
    public final void testGetValueClassName() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        assertEquals("java.lang.String", jasperField.getValueClassName());
    }

    @Test
    public final void testHasProperties() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        assertFalse(jasperField.hasProperties());
    }

    @Test
    public final void testColumnIndex() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        assertEquals(123, jasperField.getColumnIndex());
        jasperField.setColumnIndex(456);
        assertEquals(456, jasperField.getColumnIndex());
    }

    @Test
    public final void testGetPropertiesMap() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        assertNull(jasperField.getPropertiesMap());
    }

    @Test
    public final void testGetParentProperties() {
        final JasperField jasperField = new JasperField("Name", "Description", 123, String.class);
        assertNull(jasperField.getParentProperties());
    }

}
